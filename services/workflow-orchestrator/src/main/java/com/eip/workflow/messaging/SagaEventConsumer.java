package com.eip.workflow.messaging;

import com.eip.workflow.domain.enums.SagaType;
import com.eip.workflow.saga.ClaimProcessingSaga;
import com.eip.workflow.saga.PolicyIssuanceSaga;
import com.eip.workflow.service.SagaOrchestrationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventConsumer {

    private final SagaOrchestrationService orchestrationService;
    private final ObjectMapper objectMapper;

    // ── Policy Issuance saga events ───────────────────────────────────────────

    @KafkaListener(
            topics = {"policy.policies.quoted"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onPolicyQuoted(@Payload String message) {
        handle(message, "policy.policies.quoted", event -> {
            String policyId = event.path("policyId").asText();
            orchestrationService.startPolicyIssuanceSaga(policyId, message);
        });
    }

    @KafkaListener(
            topics = {"premiumcalc.results.ready"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onPremiumCalculated(@Payload String message) {
        handle(message, "premiumcalc.results.ready", event -> {
            String policyId = event.path("policyId").asText();
            orchestrationService.advanceSaga(policyId, SagaType.POLICY_ISSUANCE,
                    PolicyIssuanceSaga.PREMIUM_CALCULATED, message);
        });
    }

    @KafkaListener(
            topics = {"payment.payments.premium-received"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onPremiumReceived(@Payload String message) {
        handle(message, "payment.payments.premium-received", event -> {
            String policyId = event.path("policyId").asText();
            if (policyId != null && !policyId.isBlank()) {
                orchestrationService.advanceSaga(policyId, SagaType.POLICY_ISSUANCE,
                        PolicyIssuanceSaga.PAYMENT_COLLECTED, message);
            }
        });
    }

    @KafkaListener(
            topics = {"policy.policies.issued"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onPolicyIssued(@Payload String message) {
        handle(message, "policy.policies.issued", event -> {
            String policyId = event.path("policyId").asText();
            orchestrationService.advanceSaga(policyId, SagaType.POLICY_ISSUANCE,
                    PolicyIssuanceSaga.POLICY_ISSUED, message);
        });
    }

    @KafkaListener(
            topics = {"billing.invoices.generated"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onInvoiceGenerated(@Payload String message) {
        handle(message, "billing.invoices.generated", event -> {
            String policyId = event.path("policyId").asText();
            if (policyId != null && !policyId.isBlank()) {
                orchestrationService.advanceSaga(policyId, SagaType.POLICY_ISSUANCE,
                        PolicyIssuanceSaga.BILLING_CREATED, message);
            }
        });
    }

    @KafkaListener(
            topics = {"document.documents.generated"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onDocumentGenerated(@Payload String message) {
        handle(message, "document.documents.generated", event -> {
            String policyId = event.path("policyId").asText();
            if (policyId != null && !policyId.isBlank()) {
                orchestrationService.advanceSaga(policyId, SagaType.POLICY_ISSUANCE,
                        PolicyIssuanceSaga.DOCUMENTS_GENERATED, message);
            }
        });
    }

    // ── Claim Processing saga events ──────────────────────────────────────────

    @KafkaListener(
            topics = {"claims.claims.filed"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onClaimFiled(@Payload String message) {
        handle(message, "claims.claims.filed", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.startClaimProcessingSaga(claimId, message);
        });
    }

    @KafkaListener(
            topics = {"fraud.scores.returned"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onFraudScored(@Payload String message) {
        handle(message, "fraud.scores.returned", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                    ClaimProcessingSaga.FRAUD_SCORED, message);
        });
    }

    @KafkaListener(
            topics = {"claims.claims.coverage-verified"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onCoverageVerified(@Payload String message) {
        handle(message, "claims.claims.coverage-verified", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                    ClaimProcessingSaga.COVERAGE_VERIFIED, message);
        });
    }

    @KafkaListener(
            topics = {"claims.claims.assigned"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onAdjusterAssigned(@Payload String message) {
        handle(message, "claims.claims.assigned", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                    ClaimProcessingSaga.ADJUSTER_ASSIGNED, message);
        });
    }

    @KafkaListener(
            topics = {"claims.claims.reserve-set"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onReserveSet(@Payload String message) {
        handle(message, "claims.claims.reserve-set", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                    ClaimProcessingSaga.RESERVE_SET, message);
        });
    }

    @KafkaListener(
            topics = {"claims.claims.approved"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onClaimApproved(@Payload String message) {
        handle(message, "claims.claims.approved", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                    ClaimProcessingSaga.APPROVED, message);
        });
    }

    @KafkaListener(
            topics = {"payment.payments.claim-paid"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onClaimPaid(@Payload String message) {
        handle(message, "payment.payments.claim-paid", event -> {
            String claimId = event.path("claimId").asText();
            if (claimId != null && !claimId.isBlank()) {
                orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                        ClaimProcessingSaga.PAYMENT_INITIATED, message);
            }
        });
    }

    @KafkaListener(
            topics = {"claims.claims.closed"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onClaimClosed(@Payload String message) {
        handle(message, "claims.claims.closed", event -> {
            String claimId = event.path("claimId").asText();
            orchestrationService.advanceSaga(claimId, SagaType.CLAIM_PROCESSING,
                    ClaimProcessingSaga.CLOSED, message);
        });
    }

    // ── Failure events ────────────────────────────────────────────────────────

    @KafkaListener(
            topics = {"payment.payments.failed"},
            groupId = "cg-workflow",
            containerFactory = "workflowKafkaListenerContainerFactory"
    )
    public void onPaymentFailed(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        handle(message, topic, event -> {
            String reason = event.path("failureReason").asText("Payment failed");
            // Could be either a premium payment or claim payment failure
            String policyId = event.path("policyId").asText(null);
            String claimId  = event.path("claimId").asText(null);

            if (policyId != null && !policyId.isBlank()) {
                orchestrationService.failSaga(policyId, SagaType.POLICY_ISSUANCE,
                        PolicyIssuanceSaga.PAYMENT_COLLECTED, reason);
            } else if (claimId != null && !claimId.isBlank()) {
                orchestrationService.failSaga(claimId, SagaType.CLAIM_PROCESSING,
                        ClaimProcessingSaga.PAYMENT_INITIATED, reason);
            }
        });
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface EventHandler {
        void handle(JsonNode event) throws Exception;
    }

    private void handle(String message, String topic, EventHandler handler) {
        try {
            JsonNode event = objectMapper.readTree(message);
            handler.handle(event);
        } catch (Exception e) {
            log.error("Failed to process saga event topic={}: {}", topic, e.getMessage(), e);
        }
    }
}
