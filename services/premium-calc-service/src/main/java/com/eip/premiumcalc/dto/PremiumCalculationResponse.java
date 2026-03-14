package com.eip.premiumcalc.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PremiumCalculationResponse(
        String calculationId,
        BigDecimal basePremium,
        List<PremiumAdjustment> adjustments,
        BigDecimal finalPremium,
        String rateTableUsed,
        Instant calculatedAt
) {}
