package com.eip.fraud.dto;

import com.eip.fraud.domain.RiskLevel;
import java.time.Instant;
import java.util.List;

public record FraudScoringResponse(
        String claimId,
        int score,
        RiskLevel riskLevel,
        List<String> signals,
        Instant scoredAt,
        String recommendedAction
) {}
