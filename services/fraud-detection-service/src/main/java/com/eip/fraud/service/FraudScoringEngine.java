package com.eip.fraud.service;

import com.eip.fraud.domain.*;
import com.eip.fraud.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eip.fraud.repository.FraudScoreRepository;
import com.eip.fraud.repository.FraudCaseRepository;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudScoringEngine {

    private final VelocityService velocityService;
    private final FraudScoreRepository fraudScoreRepository;
    private final FraudCaseRepository fraudCaseRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final AtomicLong CASE_SEQUENCE = new AtomicLong(1000);

    /**
     * Score a claim for fraud using rule-based engine (Phase 1).
     * Target SLA: < 200ms
     */
    @Transactional
    public FraudScoringResponse score(FraudScoringRequest request) {
        long start = System.currentTimeMillis();
        int score = 0;
        List<String> signals = new ArrayList<>();

        // Rule 1: Customer claim velocity (30 days)
        long customerClaims30d = velocityService.getCustomerClaimCount(request.customerId(), 30);
        if (customerClaims30d >= 3) {
            score += 25;
            signals.add("HIGH_CLAIM_VELOCITY_30D:" + customerClaims30d);
        }

        // Rule 2: Address claim velocity (90 days)
        long addressClaims = velocityService.getAddressClaimCount(request.address(), 90);
        if (addressClaims >= 2) {
            score += 20;
            signals.add("HIGH_ADDRESS_VELOCITY_90D:" + addressClaims);
        }

        // Rule 3: Broker claim velocity (7 days)
        if (request.brokerId() != null) {
            long brokerClaims = velocityService.getBrokerClaimCount(request.brokerId(), 7);
            if (brokerClaims >= 10) {
                score += 15;
                signals.add("HIGH_BROKER_VELOCITY_7D:" + brokerClaims);
            }
        }

        // Rule 4: Weekend/holiday loss date (slightly elevated risk signal)
        if (request.dateOfLoss() != null) {
            DayOfWeek dow = request.dateOfLoss().getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                score += 5;
                signals.add("WEEKEND_LOSS_DATE");
            }
        }

        // Rule 5: Customer claims count (7 days) - higher threshold
        long customerClaims7d = velocityService.getCustomerClaimCount(request.customerId(), 7);
        if (customerClaims7d >= 2) {
            score += 20;
            signals.add("MULTIPLE_CLAIMS_7D:" + customerClaims7d);
        }

        // Cap at 100
        score = Math.min(score, 100);
        RiskLevel riskLevel = RiskLevel.fromScore(score);

        // Update velocity counters
        velocityService.incrementClaimCount(request.customerId(), request.address(), request.brokerId());

        // Persist score
        FraudScore fraudScore = FraudScore.builder()
                .claimId(request.claimId())
                .customerId(request.customerId())
                .policyId(request.policyId())
                .score(score)
                .riskLevel(riskLevel)
                .signals(signals.toString())
                .build();
        fraudScoreRepository.save(fraudScore);

        // Auto-open fraud case for CRITICAL scores
        if (riskLevel == RiskLevel.CRITICAL) {
            openFraudCase(request.claimId(), request.customerId(), score);
        }

        // Publish fraud.scores.returned event
        publishScoreEvent(request.claimId(), score, riskLevel, signals);

        long elapsed = System.currentTimeMillis() - start;
        log.info("Fraud scored in {}ms: claimId={}, score={}, risk={}", elapsed, request.claimId(), score, riskLevel);

        return new FraudScoringResponse(
                request.claimId(), score, riskLevel, signals, Instant.now(),
                determineAction(riskLevel)
        );
    }

    private void openFraudCase(String claimId, String customerId, int score) {
        String caseId = "FRAUD-" + Year.now().getValue() + "-" + CASE_SEQUENCE.getAndIncrement();
        FraudCase fraudCase = FraudCase.builder()
                .caseId(caseId)
                .claimId(claimId)
                .customerId(customerId)
                .caseStatus("OPEN")
                .severity(score >= 90 ? "CRITICAL" : "HIGH")
                .referredToSiu(score >= 90)
                .build();
        fraudCaseRepository.save(fraudCase);
        log.warn("Fraud case opened: {}, claimId={}, score={}", caseId, claimId, score);
    }

    private void publishScoreEvent(String claimId, int score, RiskLevel riskLevel, List<String> signals) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "claimId", claimId,
                    "score", score,
                    "riskLevel", riskLevel.name(),
                    "signals", signals,
                    "scoredAt", Instant.now().toString()
            ));
            kafkaTemplate.send("fraud.scores.returned", claimId, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to publish fraud score event: {}", e.getMessage());
        }
    }

    private String determineAction(RiskLevel level) {
        return switch (level) {
            case LOW -> "PROCEED";
            case MEDIUM -> "MANUAL_REVIEW";
            case HIGH -> "HOLD_PENDING_INVESTIGATION";
            case CRITICAL -> "REFER_TO_SIU";
        };
    }
}
