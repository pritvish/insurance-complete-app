package com.eip.premiumcalc.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record PremiumCalculationRequest(
        @NotBlank String customerId,
        @NotBlank String lineOfBusiness,
        @NotBlank String productCode,
        @NotBlank @Size(min = 2, max = 2) String stateCode,
        @NotNull @Positive BigDecimal coverageLimit,
        @NotNull @PositiveOrZero BigDecimal deductible,
        @NotNull LocalDate effectiveDate,
        @NotNull LocalDate expirationDate,
        Map<String, Object> ratingFactors
) {}
