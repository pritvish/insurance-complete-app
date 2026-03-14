package com.eip.fraud.messaging;

import com.eip.fraud.dto.FraudScoringRequest;
import com.eip.fraud.service.FraudScoringEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component @RequiredArgsConstructor @Slf4j
public class FraudEventConsumer {

    private final FraudScoringEngine fraudScoringEngine;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "claims.claims.filed", groupId = "cg-fraud-detection")
    public void handleClaimFiled(@Payload String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            FraudScoringRequest request = new FraudScoringRequest(
                    event.path("claimId").asText(),
                    event.path("customerId").asText(),
                    event.path("policyId").asText(),
                    event.path("brokerId").asText(null),
                    event.path("claimAmount").decimalValue(BigDecimal.ZERO),
                    event.path("dateOfLoss").isNull() ? LocalDate.now()
                            : LocalDate.parse(event.path("dateOfLoss").asText()),
                    event.path("address").asText(""),
                    event.path("lineOfBusiness").asText()
            );
            fraudScoringEngine.score(request);
        } catch (Exception e) {
            log.error("Error processing claim.filed for fraud scoring: {}", e.getMessage(), e);
        }
    }
}
