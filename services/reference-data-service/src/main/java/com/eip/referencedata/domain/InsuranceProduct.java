package com.eip.referencedata.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "insurance_products", indexes = {
        @Index(name = "idx_product_code", columnList = "product_code"),
        @Index(name = "idx_product_lob", columnList = "line_of_business")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "line_of_business", nullable = false)
    private String lineOfBusiness;

    @Column(name = "description")
    private String description;

    @Column(name = "min_coverage_limit", precision = 15, scale = 2)
    private BigDecimal minCoverageLimit;

    @Column(name = "max_coverage_limit", precision = 15, scale = 2)
    private BigDecimal maxCoverageLimit;

    @Column(name = "min_deductible", precision = 10, scale = 2)
    private BigDecimal minDeductible;

    @Column(name = "max_deductible", precision = 10, scale = 2)
    private BigDecimal maxDeductible;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
