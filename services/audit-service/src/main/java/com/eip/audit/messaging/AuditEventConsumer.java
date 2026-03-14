package com.eip.audit.messaging;

import com.eip.audit.domain.AuditRecord;
import com.eip.audit.service.AuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    // Consume ALL domain events and write to audit log
    @KafkaListener(
            topics = {
                    "customer.customers.registered",
                    "customer.customers.kyc-completed",
                    "customer.customers.suspended",
                    "policy.policies.quoted",
                    "policy.policies.bound",
                    "policy.policies.issued",
                    "policy.policies.endorsed",
                    "policy.policies.cancelled",
                    "claims.claims.filed",
                    "claims.claims.coverage-verified",
                    "claims.claims.assigned",
                    "claims.claims.approved",
                    "claims.claims.closed",
                    "payment.payments.premium-received",
                    "payment.payments.claim-paid",
                    "payment.payments.failed",
                    "billing.invoices.generated",
                    "billing.invoices.overdue",
                    "fraud.scores.returned",
                    "fraud.cases.opened",
                    "document.documents.generated",
                    "document.documents.signed",
                    "notification.notifications.sent"
            },
            groupId = "cg-audit",
            containerFactory = "auditKafkaListenerContainerFactory"
    )
    public void handleAllEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "correlationId", required = false) String correlationId) {
        try {
            JsonNode event = objectMapper.readTree(message);

            // Derive entity type and id from topic name (e.g., "policy.policies.issued" -> "Policy")
            String[] topicParts = topic.split("\\.");
            String entityType = topicParts.length > 0
                    ? capitalize(topicParts[0])
                    : "Unknown";
            String entityId = extractEntityId(event, entityType);

            AuditRecord record = AuditRecord.builder()
                    .correlationId(correlationId)
                    .eventType(topic)
                    .entityType(entityType)
                    .entityId(entityId)
                    .serviceSource(topicParts[0] + "-service")
                    .payload(message)
                    .occurredAt(Instant.now())
                    .build();

            auditService.recordEvent(record);
        } catch (Exception e) {
            log.error("Failed to record audit event for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private String extractEntityId(JsonNode event, String entityType) {
        // Try common ID field names
        for (String field : new String[]{"customerId", "policyId", "claimId", "paymentId",
                "invoiceId", "documentId", "caseId", "notificationId"}) {
            if (event.has(field) && !event.path(field).asText().isEmpty()) {
                return event.path(field).asText();
            }
        }
        return "unknown";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
