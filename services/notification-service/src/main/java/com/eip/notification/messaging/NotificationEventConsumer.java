package com.eip.notification.messaging;

import com.eip.notification.service.NotificationService;
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
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "policy.policies.issued",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPolicyIssued(@Payload String message,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Your Policy is Now Active",
                "<h2>Your insurance policy has been issued.</h2><p>Thank you for choosing us.</p>",
                "Your insurance policy has been issued. Thank you for choosing us.");
    }

    @KafkaListener(
            topics = "policy.policies.cancelled",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPolicyCancelled(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Your Policy Has Been Cancelled",
                "<h2>Your policy has been cancelled.</h2><p>Please contact support for more details.</p>",
                "Your insurance policy has been cancelled. Contact support for details.");
    }

    @KafkaListener(
            topics = "claims.claims.assigned",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onClaimAssigned(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Your Claim Has Been Assigned",
                "<h2>An adjuster has been assigned to your claim.</h2><p>We will contact you shortly.</p>",
                "An adjuster has been assigned to your claim. We will contact you shortly.");
    }

    @KafkaListener(
            topics = "claims.claims.approved",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onClaimApproved(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Your Claim Has Been Approved",
                "<h2>Great news! Your claim has been approved.</h2><p>Payment will be processed shortly.</p>",
                "Your insurance claim has been approved. Payment will be processed shortly.");
    }

    @KafkaListener(
            topics = "payment.payments.failed",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Payment Failed - Action Required",
                "<h2>Your payment could not be processed.</h2><p>Please update your payment method.</p>",
                "Your payment failed. Please update your payment method to keep your policy active.");
    }

    @KafkaListener(
            topics = "billing.invoices.overdue",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onInvoiceOverdue(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Invoice Overdue - Immediate Action Required",
                "<h2>Your invoice is overdue.</h2><p>Please make payment to avoid policy cancellation.</p>",
                "URGENT: Your insurance invoice is overdue. Pay now to avoid cancellation.");
    }

    @KafkaListener(
            topics = "document.documents.generated",
            groupId = "cg-notification",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDocumentGenerated(@Payload String message,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     @Header(value = "correlationId", required = false) String correlationId) {
        handleEvent(message, topic, correlationId,
                "Your Document is Ready",
                "<h2>A new document has been generated for your account.</h2><p>Log in to view it.</p>",
                "A new document has been generated for your account. Log in to view it.");
    }

    private void handleEvent(String message, String topic, String correlationId,
                              String subject, String emailBody, String smsBody) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String customerId = extractCustomerId(event);
            if (customerId == null) {
                log.warn("Could not extract customerId from topic={} payload={}", topic, message);
                return;
            }
            notificationService.sendNotification(customerId, correlationId, topic, subject, emailBody, smsBody);
        } catch (Exception e) {
            log.error("Failed to process notification for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private String extractCustomerId(JsonNode event) {
        if (event.has("customerId") && !event.path("customerId").asText().isEmpty()) {
            return event.path("customerId").asText();
        }
        return null;
    }
}
