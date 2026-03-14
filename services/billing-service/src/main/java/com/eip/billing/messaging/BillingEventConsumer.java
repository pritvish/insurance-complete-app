package com.eip.billing.messaging;

import com.eip.billing.service.BillingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component @RequiredArgsConstructor @Slf4j
public class BillingEventConsumer {

    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "policy.policies.issued", groupId = "cg-billing-policy")
    public void handlePolicyIssued(@Payload String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String policyId = event.path("policyId").asText();
            String customerId = event.path("customerId").asText();
            BigDecimal premium = event.path("premiumAmount").decimalValue(BigDecimal.ZERO);

            billingService.createBillingAccount(policyId, customerId, premium, "MONTHLY");
        } catch (Exception e) {
            log.error("Error processing policy.issued for billing: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment.payments.premium-received", groupId = "cg-billing-payment")
    public void handlePremiumReceived(@Payload String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            // In production: find invoice by policyId + amount and mark paid
            log.info("Premium received event consumed by billing service");
        } catch (Exception e) {
            log.error("Error processing premium-received for billing: {}", e.getMessage(), e);
        }
    }
}
