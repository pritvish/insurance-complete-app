package com.eip.analytics.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_metrics",
        indexes = {
                @Index(name = "idx_pm_policy_id", columnList = "policy_id", unique = true),
                @Index(name = "idx_pm_customer_id", columnList = "customer_id"),
                @Index(name = "idx_pm_broker_id", columnList = "broker_id"),
                @Index(name = "idx_pm_product_code", columnList = "product_code"),
                @Index(name = "idx_pm_status", columnList = "status"),
                @Index(name = "idx_pm_issued_at", columnList = "issued_at")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_id", nullable = false, unique = true)
    private UUID policyId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "broker_id")
    private UUID brokerId;

    @Column(name = "product_code", length = 50)
    private String productCode;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "premium_amount", precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "coverage_amount", precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "claim_count")
    @Builder.Default
    private Integer claimCount = 0;

    @Column(name = "total_claims_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalClaimsAmount = BigDecimal.ZERO;

    @Column(name = "last_event_type", length = 100)
    private String lastEventType;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Version
    private Long version;
}
