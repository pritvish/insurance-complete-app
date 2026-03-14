package com.eip.policy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * CQRS read-model (denormalized projection) for the Policy aggregate.
 * Updated by replaying events from the PolicyEvent store.
 */
@Entity
@Table(name = "policy_projections",
        indexes = {
                @Index(name = "idx_policy_proj_customer", columnList = "customer_id"),
                @Index(name = "idx_policy_proj_broker", columnList = "broker_id"),
                @Index(name = "idx_policy_proj_status", columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PolicyProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_id", unique = true, nullable = false, length = 50)
    private String policyId;

    @Column(name = "quote_id", length = 50)
    private String quoteId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "broker_id", length = 50)
    private String brokerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_of_business", nullable = false, length = 30)
    private LineOfBusiness lineOfBusiness;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.QUOTED;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "premium_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "coverage_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal coverageLimit;

    @Column(name = "deductible", nullable = false, precision = 10, scale = 2)
    private BigDecimal deductible;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "insured_name")
    private String insuredName;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;
}
