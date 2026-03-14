package com.eip.customer.messaging;

import com.eip.customer.service.CustomerService;
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
public class CustomerEventConsumer {

    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payment.payments.failed",
            groupId = "cg-customer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "correlationId", required = false) String correlationId) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String customerId = event.path("customerId").asText();
            int retryCount = event.path("retryCount").asInt(0);

            if (retryCount >= 3 && customerId != null && !customerId.isEmpty()) {
                log.warn("Suspending customer {} after {} payment failures", customerId, retryCount);
                customerService.suspendCustomer(customerId, "Repeated payment failures (count=" + retryCount + ")");
            }
        } catch (Exception e) {
            log.error("Error processing payment.failed event: {}", e.getMessage(), e);
        }
    }
}
