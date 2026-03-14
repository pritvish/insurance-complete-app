package com.eip.billing.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "invoices",
        indexes = {@Index(name = "idx_invoices_account", columnList = "account_id"),
                   @Index(name = "idx_invoices_status", columnList = "status")})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "invoice_id", unique = true, nullable = false, length = 30) private String invoiceId;
    @Column(name = "account_id", nullable = false, length = 30) private String accountId;
    @Column(name = "policy_id", nullable = false, length = 50) private String policyId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(nullable = false, length = 20) @Builder.Default private String status = "ISSUED";
    @Column(name = "issued_at") @Builder.Default private Instant issuedAt = Instant.now();
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "grace_period_ends_at") private LocalDate gracePeriodEndsAt;
}
