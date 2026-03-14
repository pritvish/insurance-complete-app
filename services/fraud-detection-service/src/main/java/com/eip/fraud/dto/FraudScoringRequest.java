package com.eip.fraud.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FraudScoringRequest(
        String claimId,
        String customerId,
        String policyId,
        String brokerId,
        BigDecimal claimAmount,
        LocalDate dateOfLoss,
        String address,
        String lineOfBusiness
) {}
