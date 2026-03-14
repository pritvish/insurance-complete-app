package com.eip.analytics.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_metrics",
        indexes = {
                @Index(name = "idx_cm_claim_id", columnList = "claim_id", unique = true),
                @Index(name = "idx_cm_policy_id", columnList = "policy_id"),
                @Index(name = "idx_cm_customer_id", columnList = "customer_id"),
                @Index(name = "idx_cm_status", columnList = "status"),
                @Index(name = "idx_cm_filed_at", columnList = "filed_at")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", nullable = false, unique = true)
    private UUID claimId;

    @Column(name = "policy_id")
    private UUID policyId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "claim_type", length = 50)
    private String claimType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "filed_amount", precision = 15, scale = 2)
    private BigDecimal filedAmount;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "filed_at")
    private Instant filedAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "processing_days_count")
    private Integer processingDaysCount;

    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore;

    @Column(name = "is_fraudulent")
    @Builder.Default
    private Boolean isFraudulent = false;

    @Column(name = "last_event_type", length = 100)
    private String lastEventType;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Version
    private Long version;
}
