package com.eip.analytics.messaging;

import com.eip.analytics.service.AnalyticsService;
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
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                    "policy.policies.quoted",
                    "policy.policies.issued",
                    "policy.policies.endorsed",
                    "policy.policies.cancelled"
            },
            groupId = "cg-analytics",
            containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void handlePolicyEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            JsonNode event = objectMapper.readTree(message);
            analyticsService.upsertPolicyMetrics(event, topic);

            // Also update broker metrics when a policy event arrives
            if (event.has("brokerId")) {
                analyticsService.upsertBrokerMetrics(event, topic);
            }
        } catch (Exception e) {
            log.error("Failed to process policy analytics event topic={}: {}", topic, e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = {
                    "claims.claims.filed",
                    "claims.claims.coverage-verified",
                    "claims.claims.assigned",
                    "claims.claims.approved",
                    "claims.claims.closed"
            },
            groupId = "cg-analytics",
            containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void handleClaimEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            JsonNode event = objectMapper.readTree(message);
            analyticsService.upsertClaimMetrics(event, topic);

            // Update broker claims count when a claim is filed
            if ("claims.claims.filed".equals(topic) && event.has("brokerId")) {
                analyticsService.upsertBrokerMetrics(event, topic);
            }
        } catch (Exception e) {
            log.error("Failed to process claim analytics event topic={}: {}", topic, e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = {"fraud.scores.returned"},
            groupId = "cg-analytics",
            containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void handleFraudScoreEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            JsonNode event = objectMapper.readTree(message);
            analyticsService.updateFraudScore(event);
        } catch (Exception e) {
            log.error("Failed to process fraud score analytics event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = {"billing.invoices.generated"},
            groupId = "cg-analytics",
            containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void handleBillingEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            JsonNode event = objectMapper.readTree(message);
            // Update broker total premium from billing events
            if (event.has("brokerId")) {
                analyticsService.upsertBrokerMetrics(event, topic);
            }
        } catch (Exception e) {
            log.error("Failed to process billing analytics event: {}", e.getMessage(), e);
        }
    }
}
