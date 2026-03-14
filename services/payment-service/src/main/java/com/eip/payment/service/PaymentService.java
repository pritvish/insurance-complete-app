package com.eip.payment.service;

import com.eip.payment.domain.*;
import com.eip.payment.dto.PremiumPaymentRequest;
import com.eip.payment.repository.OutboxEventRepository;
import com.eip.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private static final AtomicLong SEQUENCE = new AtomicLong(1000);

    @Transactional
    public Payment collectPremium(PremiumPaymentRequest request) {
        // Idempotency check - return existing payment if same key
        return paymentRepository.findByIdempotencyKey(request.idempotencyKey())
                .orElseGet(() -> createAndProcessPayment(request));
    }

    private Payment createAndProcessPayment(PremiumPaymentRequest request) {
        String paymentId = "PAY-" + Year.now().getValue() + "-" + SEQUENCE.getAndIncrement();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .idempotencyKey(request.idempotencyKey())
                .paymentType(PaymentType.PREMIUM_COLLECTION)
                .status(PaymentStatus.PROCESSING)
                .amount(request.amount())
                .currency("USD")
                .policyId(request.policyId())
                .customerId(request.customerId())
                .paymentMethod(request.paymentMethod())
                .build();

        payment = paymentRepository.save(payment);

        // In production: call Stripe API here
        // For now, simulate successful payment
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(Instant.now());
        payment.setStripePaymentIntentId("pi_simulated_" + paymentId);
        payment = paymentRepository.save(payment);

        publishEvent("Payment", paymentId, "payment.payments.premium-received", Map.of(
                "paymentId", paymentId,
                "policyId", request.policyId(),
                "customerId", request.customerId(),
                "amount", request.amount(),
                "completedAt", Instant.now().toString()
        ));

        log.info("Premium collected: paymentId={}, policyId={}", paymentId, request.policyId());
        return payment;
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    @Transactional
    public void retryFailedPayments() {
        List<Payment> failedPayments = paymentRepository
                .findByStatusAndNextRetryAtBefore(PaymentStatus.FAILED, Instant.now());

        for (Payment payment : failedPayments) {
            if (payment.getRetryCount() >= 3) {
                payment.setStatus(PaymentStatus.CANCELLED);
                log.warn("Payment {} cancelled after max retries", payment.getPaymentId());
            } else {
                payment.setRetryCount(payment.getRetryCount() + 1);
                payment.setNextRetryAt(Instant.now().plusSeconds((long) Math.pow(2, payment.getRetryCount()) * 60));
                log.info("Retrying payment {}, attempt {}", payment.getPaymentId(), payment.getRetryCount());
            }
            paymentRepository.save(payment);
        }
    }

    private void publishEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxEventRepository.save(OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    @Transactional(readOnly = true)
    public Payment getPayment(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }
}
