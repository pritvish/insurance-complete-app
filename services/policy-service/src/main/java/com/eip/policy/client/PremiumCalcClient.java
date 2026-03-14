package com.eip.policy.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@FeignClient(name = "premium-calc-service", url = "${services.premium-calc.url:http://localhost:8083}")
public interface PremiumCalcClient {

    @PostMapping("/api/v1/premium/calculate")
    PremiumCalculationResult calculate(@RequestBody PremiumCalculationRequest request);

    record PremiumCalculationRequest(
            String customerId,
            String lineOfBusiness,
            String productCode,
            String stateCode,
            BigDecimal coverageLimit,
            BigDecimal deductible,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            Map<String, Object> ratingFactors
    ) {}

    record PremiumCalculationResult(
            String calculationId,
            BigDecimal basePremium,
            BigDecimal finalPremium,
            String rateTableUsed
    ) {}
}
