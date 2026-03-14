package com.eip.payment.messaging;

import com.eip.payment.domain.OutboxEvent;
import com.eip.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component @RequiredArgsConstructor @Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 100)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                event.setStatus("PROCESSED");
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                event.setStatus("FAILED");
                outboxEventRepository.save(event);
            }
        }
    }
}
