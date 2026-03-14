package com.eip.customer.messaging;

import com.eip.customer.domain.OutboxEvent;
import com.eip.customer.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 100)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                event.setStatus("PROCESSED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
                log.debug("Published outbox event: {} for aggregate: {}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                event.setStatus("FAILED");
                outboxEventRepository.save(event);
            }
        }
    }
}
