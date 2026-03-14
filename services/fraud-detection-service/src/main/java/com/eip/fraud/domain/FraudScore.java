package com.eip.fraud.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "fraud_scores",
        indexes = {@Index(name = "idx_fraud_scores_claim", columnList = "claim_id"),
                   @Index(name = "idx_fraud_scores_customer", columnList = "customer_id")})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FraudScore {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "claim_id", nullable = false, length = 30) private String claimId;
    @Column(name = "customer_id", nullable = false, length = 50) private String customerId;
    @Column(name = "policy_id", length = 50) private String policyId;
    @Column(nullable = false) private Integer score;
    @Enumerated(EnumType.STRING) @Column(name = "risk_level", nullable = false, length = 20) private RiskLevel riskLevel;
    @Column(columnDefinition = "TEXT") private String signals; // JSON array
    @Column(name = "scored_at", nullable = false, updatable = false) @Builder.Default private Instant scoredAt = Instant.now();
    @Column(name = "model_version", length = 20) @Builder.Default private String modelVersion = "RULE_BASED_v1";
}
