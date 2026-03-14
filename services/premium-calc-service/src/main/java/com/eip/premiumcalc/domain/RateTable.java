package com.eip.premiumcalc.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rate_tables",
        indexes = {
                @Index(name = "idx_rate_tables_lob_state", columnList = "line_of_business, state_code, effective_date"),
                @Index(name = "idx_rate_tables_code", columnList = "table_code")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "table_code", unique = true, nullable = false, length = 50)
    private String tableCode;

    @Column(name = "line_of_business", nullable = false, length = 30)
    private String lineOfBusiness;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    /**
     * JSON-encoded rate factors map. Keys: territory, creditScore, claimHistory, vehicleAge, etc.
     * Example: {"baseRate": 0.025, "territoryFactor_urban": 1.2, "creditGood": 0.95}
     */
    @Column(name = "rate_factors", columnDefinition = "TEXT", nullable = false)
    private String rateFactors;

    @Column(name = "base_rate", nullable = false, precision = 8, scale = 6)
    private BigDecimal baseRate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Version
    private Long version;
}
