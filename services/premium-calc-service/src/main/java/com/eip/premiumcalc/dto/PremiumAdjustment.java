package com.eip.premiumcalc.dto;

import java.math.BigDecimal;

public record PremiumAdjustment(
        String factor,
        String description,
        BigDecimal adjustmentAmount,
        String adjustmentType  // DISCOUNT or SURCHARGE
) {}
