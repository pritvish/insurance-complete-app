package com.eip.policy.messaging;

import com.eip.policy.service.PolicyCommandService;
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
public class PolicyEventConsumer {

    private final PolicyCommandService policyCommandService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.payments.premium-received", groupId = "cg-policy-payment")
    public void handlePremiumReceived(
            @Payload String message,
            @Header(value = "correlationId", required = false) String correlationId) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String policyId = event.path("policyId").asText();
            if (policyId != null && !policyId.isEmpty()) {
                log.info("Premium received for policy {}, issuing policy", policyId);
                policyCommandService.issuePolicy(policyId, correlationId);
            }
        } catch (Exception e) {
            log.error("Error processing premium-received event: {}", e.getMessage(), e);
        }
    }
}
