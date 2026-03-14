package com.eip.policy.dto;

import com.eip.policy.domain.LineOfBusiness;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteRequest(
        @NotBlank String customerId,
        String brokerId,
        @NotNull LineOfBusiness lineOfBusiness,
        @NotBlank String productCode,
        @NotBlank @Size(min = 2, max = 2) String stateCode,
        @NotNull @Future LocalDate effectiveDate,
        @NotNull LocalDate expirationDate,
        @NotNull @Positive BigDecimal coverageLimit,
        @NotNull @PositiveOrZero BigDecimal deductible,
        String insuredName
) {}
