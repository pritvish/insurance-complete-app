package com.eip.policy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record QuoteResponse(
        String quoteId,
        String policyId,
        BigDecimal premiumAmount,
        BigDecimal coverageLimit,
        BigDecimal deductible,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        Instant validUntil,
        String lineOfBusiness,
        String stateCode
) {}
