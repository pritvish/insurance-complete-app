package com.eip.customer.service;

import com.eip.customer.domain.OutboxEvent;
import com.eip.customer.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Save an outbox event within the CURRENT transaction.
     * The event is written atomically with the business state change.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent saveEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadJson)
                .status("PENDING")
                .build();

        return outboxEventRepository.save(event);
    }
}
