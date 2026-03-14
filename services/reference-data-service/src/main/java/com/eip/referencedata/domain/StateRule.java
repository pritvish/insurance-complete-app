package com.eip.referencedata.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "state_rules",
       uniqueConstraints = @UniqueConstraint(columnNames = {"state_code", "line_of_business"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "state_code", nullable = false, length = 5)
    private String stateCode;

    @Column(name = "state_name", nullable = false)
    private String stateName;

    @Column(name = "line_of_business", nullable = false)
    private String lineOfBusiness;

    @Column(name = "min_liability_limit", precision = 15, scale = 2)
    private BigDecimal minLiabilityLimit;

    @Column(name = "mandatory_coverage_types")
    private String mandatoryCoverageTypes; // JSON array

    @Column(name = "regulatory_surcharge_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal regulatorySurchargeRate = BigDecimal.ZERO;

    @Column(name = "no_fault_state")
    @Builder.Default
    private boolean noFaultState = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
