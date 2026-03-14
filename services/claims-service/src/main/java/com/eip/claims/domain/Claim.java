package com.eip.claims.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims",
        indexes = {
                @Index(name = "idx_claims_policy", columnList = "policy_id"),
                @Index(name = "idx_claims_customer", columnList = "customer_id"),
                @Index(name = "idx_claims_status", columnList = "status"),
                @Index(name = "idx_claims_adjuster", columnList = "assigned_adjuster_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "claim_id", unique = true, nullable = false, length = 30)
    private String claimId;

    @Column(name = "policy_id", nullable = false, length = 50)
    private String policyId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "assigned_adjuster_id", length = 50)
    private String assignedAdjusterId;

    @Column(name = "line_of_business", nullable = false, length = 30)
    private String lineOfBusiness;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.FNOL_RECEIVED;

    @Column(name = "date_of_loss", nullable = false)
    private LocalDate dateOfLoss;

    @Column(name = "date_reported", nullable = false)
    @Builder.Default
    private LocalDate dateReported = LocalDate.now();

    @Column(name = "fnol_submitted_at", nullable = false)
    @Builder.Default
    private Instant fnolSubmittedAt = Instant.now();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "claim_amount", precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "reserve_amount", precision = 12, scale = 2)
    private BigDecimal reserveAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "coverage_verified")
    @Builder.Default
    private Boolean coverageVerified = false;

    @Column(name = "fraud_score")
    private Integer fraudScore;

    @Column(name = "fraud_score_returned_at")
    private Instant fraudScoreReturnedAt;

    @Column(name = "litigation_flagged")
    @Builder.Default
    private Boolean litigationFlagged = false;

    @Column(name = "subrogation_applicable")
    @Builder.Default
    private Boolean subrogationApplicable = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
