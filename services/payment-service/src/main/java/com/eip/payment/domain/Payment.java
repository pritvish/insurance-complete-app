package com.eip.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payments_policy", columnList = "policy_id"),
                @Index(name = "idx_payments_claim", columnList = "claim_id"),
                @Index(name = "idx_payments_status", columnList = "status"),
                @Index(name = "idx_payments_idem_key", columnList = "idempotency_key")
        })
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;

    @Column(name = "payment_id", unique = true, nullable = false, length = 30) private String paymentId;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30) private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) @Builder.Default private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;

    @Column(length = 3) @Builder.Default private String currency = "USD";

    @Column(name = "policy_id", length = 50) private String policyId;
    @Column(name = "claim_id", length = 30) private String claimId;
    @Column(name = "customer_id", length = 50) private String customerId;
    @Column(name = "broker_id", length = 50) private String brokerId;

    @Column(name = "payment_method", nullable = false, length = 20) private String paymentMethod;

    @Column(name = "stripe_payment_intent_id") private String stripePaymentIntentId;
    @Column(name = "stripe_charge_id") private String stripeChargeId;

    @Column(name = "retry_count") @Builder.Default private Integer retryCount = 0;
    @Column(name = "next_retry_at") private Instant nextRetryAt;
    @Column(name = "failure_reason", columnDefinition = "TEXT") private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false) @Builder.Default private Instant createdAt = Instant.now();
    @Column(name = "completed_at") private Instant completedAt;

    @Version private Long version;
}
