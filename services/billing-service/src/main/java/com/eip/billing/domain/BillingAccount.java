package com.eip.billing.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "billing_accounts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BillingAccount {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "account_id", unique = true, nullable = false, length = 30) private String accountId;
    @Column(name = "policy_id", unique = true, nullable = false, length = 50) private String policyId;
    @Column(name = "customer_id", nullable = false, length = 50) private String customerId;
    @Column(name = "billing_frequency", nullable = false, length = 20) private String billingFrequency;
    @Column(name = "next_due_date") private LocalDate nextDueDate;
    @Column(name = "outstanding_balance", precision = 12, scale = 2) @Builder.Default private BigDecimal outstandingBalance = BigDecimal.ZERO;
    @Column(nullable = false, length = 20) @Builder.Default private String status = "ACTIVE";
    @Column(name = "created_at", nullable = false, updatable = false) @Builder.Default private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) @Builder.Default private Instant updatedAt = Instant.now();
}
