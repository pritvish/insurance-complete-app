package com.eip.claims.messaging;

import com.eip.claims.service.ClaimsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component @RequiredArgsConstructor @Slf4j
public class ClaimsEventConsumer {

    private final ClaimsService claimsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "fraud.scores.returned", groupId = "cg-claims-fraud")
    public void handleFraudScore(@Payload String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String claimId = event.path("claimId").asText();
            int score = event.path("score").asInt(0);
            if (!claimId.isEmpty()) {
                claimsService.updateFraudScore(claimId, score);
            }
        } catch (Exception e) {
            log.error("Error processing fraud score event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment.payments.claim-paid", groupId = "cg-claims-payment")
    public void handleClaimPaid(@Payload String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String claimId = event.path("claimId").asText();
            if (!claimId.isEmpty()) {
                claimsService.closeClaim(claimId);
            }
        } catch (Exception e) {
            log.error("Error processing claim-paid event: {}", e.getMessage(), e);
        }
    }
}
