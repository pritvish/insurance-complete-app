package com.eip.workflow.controller;

import com.eip.workflow.domain.SagaInstance;
import com.eip.workflow.domain.SagaStep;
import com.eip.workflow.domain.enums.SagaStatus;
import com.eip.workflow.service.SagaOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final SagaOrchestrationService orchestrationService;

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public record SagaStepDto(
            UUID id,
            String stepName,
            int stepOrder,
            String stepStatus,
            Instant startedAt,
            Instant completedAt,
            int retryCount
    ) {}

    public record SagaInstanceDto(
            UUID id,
            String sagaType,
            String sagaStatus,
            String correlationId,
            String currentStep,
            Instant startedAt,
            Instant completedAt,
            List<SagaStepDto> steps
    ) {}

    public record StartPolicyIssuanceRequest(String policyId, String payload) {}
    public record StartClaimProcessingRequest(String claimId, String payload) {}

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping("/sagas")
    @PreAuthorize("hasAnyRole('WORKFLOW_ADMIN', 'ADMIN')")
    public ResponseEntity<Page<SagaInstanceDto>> getSagas(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SagaStatus sagaStatus = status != null ? SagaStatus.valueOf(status.toUpperCase()) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        Page<SagaInstanceDto> result = orchestrationService.getSagas(sagaStatus, pageable)
                .map(this::toDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sagas/{id}")
    @PreAuthorize("hasAnyRole('WORKFLOW_ADMIN', 'ADMIN')")
    public ResponseEntity<SagaInstanceDto> getSaga(@PathVariable UUID id) {
        return ResponseEntity.ok(toDto(orchestrationService.getSagaById(id)));
    }

    @PostMapping("/sagas/policy-issuance")
    @PreAuthorize("hasRole('WORKFLOW_ADMIN')")
    public ResponseEntity<SagaInstanceDto> startPolicyIssuance(
            @RequestBody StartPolicyIssuanceRequest request) {
        SagaInstance instance = orchestrationService.startPolicyIssuanceSaga(
                request.policyId(), request.payload());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(instance));
    }

    @PostMapping("/sagas/claim-processing")
    @PreAuthorize("hasRole('WORKFLOW_ADMIN')")
    public ResponseEntity<SagaInstanceDto> startClaimProcessing(
            @RequestBody StartClaimProcessingRequest request) {
        SagaInstance instance = orchestrationService.startClaimProcessingSaga(
                request.claimId(), request.payload());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(instance));
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private SagaInstanceDto toDto(SagaInstance instance) {
        List<SagaStepDto> steps = instance.getSteps().stream()
                .map(this::toStepDto)
                .toList();
        return new SagaInstanceDto(
                instance.getId(),
                instance.getSagaType().name(),
                instance.getSagaStatus().name(),
                instance.getCorrelationId(),
                instance.getCurrentStep(),
                instance.getStartedAt(),
                instance.getCompletedAt(),
                steps
        );
    }

    private SagaStepDto toStepDto(SagaStep step) {
        return new SagaStepDto(
                step.getId(),
                step.getStepName(),
                step.getStepOrder(),
                step.getStepStatus().name(),
                step.getStartedAt(),
                step.getCompletedAt(),
                step.getRetryCount()
        );
    }
}
