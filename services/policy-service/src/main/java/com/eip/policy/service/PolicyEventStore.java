package com.eip.policy.service;

import com.eip.policy.domain.OutboxEvent;
import com.eip.policy.domain.PolicyEvent;
import com.eip.policy.repository.OutboxEventRepository;
import com.eip.policy.repository.PolicyEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyEventStore {

    private final PolicyEventRepository policyEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Append an event to the event store and write to outbox, atomically.
     * Must be called within an existing transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public PolicyEvent appendEvent(String policyId, String eventType, Object payload, String correlationId) {
        // Get next event version (optimistic concurrency)
        long nextVersion = policyEventRepository
                .findTopByPolicyIdOrderByEventVersionDesc(policyId)
                .map(e -> e.getEventVersion() + 1)
                .orElse(1L);

        String payloadJson = serializePayload(payload);

        PolicyEvent event = PolicyEvent.builder()
                .policyId(policyId)
                .eventType(eventType)
                .eventVersion(nextVersion)
                .payload(payloadJson)
                .correlationId(correlationId)
                .build();

        PolicyEvent saved = policyEventRepository.save(event);

        // Write to outbox (Kafka publishing happens async via OutboxPublisher)
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("Policy")
                .aggregateId(policyId)
                .eventType(eventType)
                .payload(payloadJson)
                .build();
        outboxEventRepository.save(outboxEvent);

        log.debug("Event appended: type={}, policyId={}, version={}", eventType, policyId, nextVersion);
        return saved;
    }

    public List<PolicyEvent> getEvents(String policyId) {
        return policyEventRepository.findByPolicyIdOrderByEventVersionAsc(policyId);
    }

    private String serializePayload(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}
