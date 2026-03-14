package com.eip.premiumcalc.service;

import com.eip.premiumcalc.domain.RateTable;
import com.eip.premiumcalc.dto.*;
import com.eip.premiumcalc.exception.RateTableNotFoundException;
import com.eip.premiumcalc.repository.RateTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingEngine {

    private final RateTableRepository rateTableRepository;

    /**
     * Calculate premium for a given risk profile.
     * Target SLA: sub-second response (rate tables cached).
     */
    public PremiumCalculationResponse calculate(PremiumCalculationRequest request) {
        long start = System.currentTimeMillis();

        RateTable rateTable = getRateTable(request.lineOfBusiness(), request.stateCode(), request.effectiveDate());

        // Base premium = coverage limit × annual base rate × pro-rata days factor
        long policyDays = ChronoUnit.DAYS.between(request.effectiveDate(), request.expirationDate());
        double prorataFactor = policyDays / 365.0;

        BigDecimal basePremium = request.coverageLimit()
                .multiply(rateTable.getBaseRate())
                .multiply(BigDecimal.valueOf(prorataFactor))
                .setScale(2, RoundingMode.HALF_UP);

        List<PremiumAdjustment> adjustments = new ArrayList<>();
        BigDecimal totalAdjustment = BigDecimal.ZERO;

        // Credit score factor
        if (request.ratingFactors() != null) {
            Object creditScoreObj = request.ratingFactors().get("creditScore");
            if (creditScoreObj instanceof Number creditScore) {
                int score = ((Number) creditScore).intValue();
                BigDecimal creditFactor;
                String description;
                if (score >= 750) {
                    creditFactor = basePremium.multiply(BigDecimal.valueOf(-0.10));
                    description = "Excellent credit discount (750+)";
                } else if (score >= 700) {
                    creditFactor = basePremium.multiply(BigDecimal.valueOf(-0.05));
                    description = "Good credit discount (700-749)";
                } else if (score < 600) {
                    creditFactor = basePremium.multiply(BigDecimal.valueOf(0.15));
                    description = "Poor credit surcharge (<600)";
                } else {
                    creditFactor = BigDecimal.ZERO;
                    description = "Standard credit (600-699)";
                }
                if (creditFactor.compareTo(BigDecimal.ZERO) != 0) {
                    String type = creditFactor.compareTo(BigDecimal.ZERO) < 0 ? "DISCOUNT" : "SURCHARGE";
                    adjustments.add(new PremiumAdjustment("CREDIT_SCORE", description,
                            creditFactor.setScale(2, RoundingMode.HALF_UP), type));
                    totalAdjustment = totalAdjustment.add(creditFactor);
                }
            }

            // Prior claims surcharge
            Object claimsObj = request.ratingFactors().get("priorClaims");
            if (claimsObj instanceof Number priorClaims && ((Number) priorClaims).intValue() > 0) {
                int claims = ((Number) priorClaims).intValue();
                BigDecimal surcharge = basePremium.multiply(BigDecimal.valueOf(claims * 0.08));
                adjustments.add(new PremiumAdjustment("PRIOR_CLAIMS",
                        "Prior claims surcharge (" + claims + " claims)",
                        surcharge.setScale(2, RoundingMode.HALF_UP), "SURCHARGE"));
                totalAdjustment = totalAdjustment.add(surcharge);
            }

            // Deductible discount
            BigDecimal baseDeductible = BigDecimal.valueOf(500);
            if (request.deductible().compareTo(baseDeductible) > 0) {
                BigDecimal deductibleDiscount = basePremium.multiply(BigDecimal.valueOf(0.05));
                adjustments.add(new PremiumAdjustment("HIGH_DEDUCTIBLE",
                        "Higher deductible discount",
                        deductibleDiscount.negate().setScale(2, RoundingMode.HALF_UP), "DISCOUNT"));
                totalAdjustment = totalAdjustment.subtract(deductibleDiscount);
            }
        }

        BigDecimal finalPremium = basePremium.add(totalAdjustment).max(BigDecimal.valueOf(50));
        finalPremium = finalPremium.setScale(2, RoundingMode.HALF_UP);

        long elapsed = System.currentTimeMillis() - start;
        log.debug("Premium calculated in {}ms for {}/{}", elapsed, request.lineOfBusiness(), request.stateCode());

        return new PremiumCalculationResponse(
                UUID.randomUUID().toString(),
                basePremium,
                adjustments,
                finalPremium,
                rateTable.getTableCode(),
                Instant.now()
        );
    }

    @Cacheable(value = "rate-tables", key = "#lob + ':' + #stateCode + ':' + #effectiveDate")
    public RateTable getRateTable(String lob, String stateCode, LocalDate effectiveDate) {
        return rateTableRepository
                .findTopByLineOfBusinessAndStateCodeAndIsActiveTrueAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        lob, stateCode, effectiveDate)
                .orElseThrow(() -> new RateTableNotFoundException(
                        "No active rate table found for LOB=" + lob + " state=" + stateCode));
    }
}
