package com.eip.payment.repository;

import com.eip.payment.domain.Payment;
import com.eip.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByIdempotencyKey(String key);
    Optional<Payment> findByClaimId(String claimId);
    List<Payment> findByStatusAndNextRetryAtBefore(PaymentStatus status, Instant time);
}
