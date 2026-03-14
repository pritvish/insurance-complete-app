package com.eip.fraud.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "fraud_cases")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FraudCase {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "case_id", unique = true, nullable = false, length = 30) private String caseId;
    @Column(name = "claim_id", nullable = false, length = 30) private String claimId;
    @Column(name = "customer_id", nullable = false, length = 50) private String customerId;
    @Column(name = "case_status", nullable = false, length = 30) @Builder.Default private String caseStatus = "OPEN";
    @Column(length = 20) private String severity;
    @Column(name = "referred_to_siu") @Builder.Default private Boolean referredToSiu = false;
    @Column(name = "opened_at", nullable = false, updatable = false) @Builder.Default private Instant openedAt = Instant.now();
    @Column(name = "closed_at") private Instant closedAt;
    @Column(name = "investigator_id", length = 50) private String investigatorId;
    @Column(columnDefinition = "TEXT") private String findings;
}
