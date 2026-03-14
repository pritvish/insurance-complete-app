package com.eip.workflow.service;

import com.eip.workflow.domain.SagaInstance;
import com.eip.workflow.domain.SagaStep;
import com.eip.workflow.domain.enums.SagaStatus;
import com.eip.workflow.domain.enums.SagaType;
import com.eip.workflow.domain.enums.StepStatus;
import com.eip.workflow.repository.SagaInstanceRepository;
import com.eip.workflow.repository.SagaStepRepository;
import com.eip.workflow.saga.ClaimProcessingSaga;
import com.eip.workflow.saga.PolicyIssuanceSaga;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SagaOrchestrationService {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaStepRepository sagaStepRepository;
    private final PolicyIssuanceSaga policyIssuanceSaga;
    private final ClaimProcessingSaga claimProcessingSaga;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ── Start sagas ───────────────────────────────────────────────────────────

    public SagaInstance startPolicyIssuanceSaga(String policyId, String payload) {
        // Idempotency: return existing if already started
        return sagaInstanceRepository
                .findByCorrelationIdAndSagaType(policyId, SagaType.POLICY_ISSUANCE)
                .orElseGet(() -> {
                    SagaInstance instance = SagaInstance.builder()
                            .sagaType(SagaType.POLICY_ISSUANCE)
                            .sagaStatus(SagaStatus.STARTED)
                            .correlationId(policyId)
                            .currentStep(PolicyIssuanceSaga.QUOTE_CREATED)
                            .payload(payload)
                            .build();

                    createSteps(instance, policyIssuanceSaga.getStepNames(), payload);
                    SagaInstance saved = sagaInstanceRepository.save(instance);

                    // Mark first step in-progress and emit command
                    markStepInProgress(saved, PolicyIssuanceSaga.QUOTE_CREATED, payload);
                    String commandTopic = policyIssuanceSaga.getNextCommandTopic(PolicyIssuanceSaga.QUOTE_CREATED);
                    if (commandTopic != null) {
                        emitCommand(commandTopic, policyId, payload);
                    }

                    log.info("Started PolicyIssuanceSaga for policyId={}", policyId);
                    return saved;
                });
    }

    public SagaInstance startClaimProcessingSaga(String claimId, String payload) {
        return sagaInstanceRepository
                .findByCorrelationIdAndSagaType(claimId, SagaType.CLAIM_PROCESSING)
                .orElseGet(() -> {
                    SagaInstance instance = SagaInstance.builder()
                            .sagaType(SagaType.CLAIM_PROCESSING)
                            .sagaStatus(SagaStatus.STARTED)
                            .correlationId(claimId)
                            .currentStep(ClaimProcessingSaga.FNOL_RECEIVED)
                            .payload(payload)
                            .build();

                    createSteps(instance, claimProcessingSaga.getStepNames(), payload);
                    SagaInstance saved = sagaInstanceRepository.save(instance);

                    markStepInProgress(saved, ClaimProcessingSaga.FNOL_RECEIVED, payload);
                    String commandTopic = claimProcessingSaga.getNextCommandTopic(ClaimProcessingSaga.FNOL_RECEIVED);
                    if (commandTopic != null) {
                        emitCommand(commandTopic, claimId, payload);
                    }

                    log.info("Started ClaimProcessingSaga for claimId={}", claimId);
                    return saved;
                });
    }

    // ── Advance saga ──────────────────────────────────────────────────────────

    public void advanceSaga(String correlationId, SagaType sagaType, String completedStep, String outputPayload) {
        SagaInstance instance = findSaga(correlationId, sagaType);

        if (instance.getSagaStatus() == SagaStatus.COMPLETED
                || instance.getSagaStatus() == SagaStatus.COMPENSATED
                || instance.getSagaStatus() == SagaStatus.COMPENSATING) {
            log.warn("advanceSaga: saga already in terminal/compensating state. correlationId={} status={}",
                    correlationId, instance.getSagaStatus());
            return;
        }

        // Mark the completed step
        sagaStepRepository.findBySagaInstanceAndStepName(instance, completedStep).ifPresent(step -> {
            step.setStepStatus(StepStatus.COMPLETED);
            step.setOutputPayload(outputPayload);
            step.setCompletedAt(Instant.now());
            sagaStepRepository.save(step);
        });

        // Determine next step
        String nextStep = switch (sagaType) {
            case POLICY_ISSUANCE -> policyIssuanceSaga.getNextStep(completedStep);
            case CLAIM_PROCESSING -> claimProcessingSaga.getNextStep(completedStep);
        };

        if (nextStep == null) {
            // Terminal — saga complete
            instance.setSagaStatus(SagaStatus.COMPLETED);
            instance.setCompletedAt(Instant.now());
            sagaInstanceRepository.save(instance);
            log.info("Saga COMPLETED. correlationId={} type={}", correlationId, sagaType);
            return;
        }

        // Advance
        instance.setSagaStatus(SagaStatus.IN_PROGRESS);
        instance.setCurrentStep(nextStep);
        sagaInstanceRepository.save(instance);

        markStepInProgress(instance, nextStep, outputPayload);

        String commandTopic = switch (sagaType) {
            case POLICY_ISSUANCE -> policyIssuanceSaga.getNextCommandTopic(completedStep);
            case CLAIM_PROCESSING -> claimProcessingSaga.getNextCommandTopic(completedStep);
        };
        if (commandTopic != null) {
            emitCommand(commandTopic, correlationId, outputPayload);
        }

        log.info("Saga advanced to step={} correlationId={} type={}", nextStep, correlationId, sagaType);
    }

    // ── Fail / compensate saga ────────────────────────────────────────────────

    public void failSaga(String correlationId, SagaType sagaType, String failedStep, String reason) {
        SagaInstance instance = findSaga(correlationId, sagaType);

        sagaStepRepository.findBySagaInstanceAndStepName(instance, failedStep).ifPresent(step -> {
            step.setStepStatus(StepStatus.FAILED);
            step.setFailureReason(reason);
            step.setCompletedAt(Instant.now());
            sagaStepRepository.save(step);
        });

        instance.setSagaStatus(SagaStatus.COMPENSATING);
        instance.setFailureReason(reason);
        sagaInstanceRepository.save(instance);
        log.warn("Saga FAILED at step={} correlationId={} type={} reason={}", failedStep, correlationId, sagaType, reason);

        // Emit compensating commands for all previously completed steps (reverse order)
        List<SagaStep> steps = sagaStepRepository.findBySagaInstanceOrderByStepOrderAsc(instance);
        for (int i = steps.size() - 1; i >= 0; i--) {
            SagaStep step = steps.get(i);
            if (step.getStepStatus() == StepStatus.COMPLETED) {
                String compensationTopic = switch (sagaType) {
                    case POLICY_ISSUANCE -> policyIssuanceSaga.getCompensationTopic(step.getStepName());
                    case CLAIM_PROCESSING -> claimProcessingSaga.getCompensationTopic(step.getStepName());
                };
                if (compensationTopic != null) {
                    emitCommand(compensationTopic, correlationId, instance.getPayload());
                    step.setStepStatus(StepStatus.COMPENSATED);
                    sagaStepRepository.save(step);
                    log.info("Emitted compensation for step={} topic={}", step.getStepName(), compensationTopic);
                }
            }
        }

        instance.setSagaStatus(SagaStatus.COMPENSATED);
        instance.setCompletedAt(Instant.now());
        sagaInstanceRepository.save(instance);
        log.info("Saga COMPENSATED. correlationId={} type={}", correlationId, sagaType);
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SagaInstance> getSagas(SagaStatus status, Pageable pageable) {
        if (status != null) {
            return sagaInstanceRepository.findBySagaStatus(status, pageable);
        }
        return sagaInstanceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public SagaInstance getSagaById(UUID id) {
        return sagaInstanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SagaInstance not found: " + id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SagaInstance findSaga(String correlationId, SagaType sagaType) {
        return sagaInstanceRepository.findByCorrelationIdAndSagaType(correlationId, sagaType)
                .orElseThrow(() -> new EntityNotFoundException(
                        "SagaInstance not found for correlationId=" + correlationId + " type=" + sagaType));
    }

    private void createSteps(SagaInstance instance, List<String> stepNames, String inputPayload) {
        for (int i = 0; i < stepNames.size(); i++) {
            SagaStep step = SagaStep.builder()
                    .sagaInstance(instance)
                    .stepName(stepNames.get(i))
                    .stepOrder(i + 1)
                    .stepStatus(StepStatus.PENDING)
                    .inputPayload(i == 0 ? inputPayload : null)
                    .build();
            instance.getSteps().add(step);
        }
    }

    private void markStepInProgress(SagaInstance instance, String stepName, String inputPayload) {
        sagaStepRepository.findBySagaInstanceAndStepName(instance, stepName).ifPresent(step -> {
            step.setStepStatus(StepStatus.IN_PROGRESS);
            step.setStartedAt(Instant.now());
            step.setInputPayload(inputPayload);
            sagaStepRepository.save(step);
        });
    }

    private void emitCommand(String topic, String key, String payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to emit command to topic={} key={}: {}", topic, key, ex.getMessage());
                    } else {
                        log.debug("Command emitted to topic={} key={}", topic, key);
                    }
                });
    }
}
