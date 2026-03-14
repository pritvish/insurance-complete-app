package com.eip.broker.dto;

import java.math.BigDecimal;

public record BrokerPortfolioSummary(
        String brokerId,
        String brokerName,
        String agencyName,
        int ytdPolicyCount,
        BigDecimal ytdPremiumVolume,
        BigDecimal commissionRate,
        BigDecimal estimatedYtdCommission,
        String status
) {}
