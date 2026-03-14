package com.eip.claims.service;

import com.eip.claims.domain.*;
import com.eip.claims.dto.*;
import com.eip.claims.exception.ClaimNotFoundException;
import com.eip.claims.exception.InvalidClaimStateException;
import com.eip.claims.repository.ClaimRepository;
import com.eip.claims.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimsService {

    private final ClaimRepository claimRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private static final AtomicLong SEQUENCE = new AtomicLong(1000);
    private static final BigDecimal FOUR_EYES_THRESHOLD = new BigDecimal("10000");

    @Transactional
    public Claim fileClaim(FnolRequest request) {
        String claimId = generateClaimId(request.lineOfBusiness());

        Claim claim = Claim.builder()
                .claimId(claimId)
                .policyId(request.policyId())
                .customerId(request.customerId())
                .lineOfBusiness(request.lineOfBusiness())
                .status(ClaimStatus.FNOL_RECEIVED)
                .dateOfLoss(request.dateOfLoss())
                .description(request.description())
                .claimAmount(request.estimatedAmount())
                .build();

        Claim saved = claimRepository.save(claim);
        publishEvent("Claim", claimId, "claims.claims.filed", Map.of(
                "claimId", claimId,
                "policyId", request.policyId(),
                "customerId", request.customerId(),
                "lineOfBusiness", request.lineOfBusiness(),
                "claimAmount", request.estimatedAmount() != null ? request.estimatedAmount() : 0,
                "dateOfLoss", request.dateOfLoss().toString(),
                "filedAt", Instant.now().toString()
        ));

        log.info("FNOL received, claimId={}", claimId);
        return saved;
    }

    @Transactional
    public Claim updateFraudScore(String claimId, Integer fraudScore) {
        Claim claim = getByClaimId(claimId);
        claim.setFraudScore(fraudScore);
        claim.setFraudScoreReturnedAt(Instant.now());

        if (fraudScore > 80) {
            claim.setStatus(ClaimStatus.UNDER_INVESTIGATION);
            log.warn("Claim {} flagged for fraud investigation, score={}", claimId, fraudScore);
        } else if (claim.getStatus() == ClaimStatus.FRAUD_REVIEW) {
            claim.setStatus(ClaimStatus.COVERAGE_VERIFICATION);
        }

        return claimRepository.save(claim);
    }

    @Transactional
    public Claim verifyCoverage(String claimId, boolean covered) {
        Claim claim = getByClaimId(claimId);
        claim.setCoverageVerified(covered);
        claim.setStatus(covered ? ClaimStatus.ASSIGNED : ClaimStatus.DENIED);

        publishEvent("Claim", claimId, "claims.claims.coverage-verified", Map.of(
                "claimId", claimId,
                "covered", covered
        ));
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim assignAdjuster(ClaimAssignmentRequest request) {
        Claim claim = getByClaimId(request.claimId());
        claim.setAssignedAdjusterId(request.adjusterId());
        claim.setStatus(ClaimStatus.ASSIGNED);

        publishEvent("Claim", request.claimId(), "claims.claims.assigned", Map.of(
                "claimId", request.claimId(),
                "adjusterId", request.adjusterId()
        ));
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim setReserve(ReserveRequest request) {
        Claim claim = getByClaimId(request.claimId());
        claim.setReserveAmount(request.reserveAmount());
        claim.setStatus(ClaimStatus.RESERVE_SET);
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim approveClaim(String claimId, BigDecimal approvedAmount, String approvedBy) {
        Claim claim = getByClaimId(claimId);

        // 4-eyes check: amounts > $10K require supervisor approval (enforced by RBAC at API layer)
        if (approvedAmount.compareTo(FOUR_EYES_THRESHOLD) > 0) {
            log.info("Claim {} approval for ${} requires supervisor sign-off", claimId, approvedAmount);
        }

        claim.setClaimAmount(approvedAmount);
        claim.setStatus(ClaimStatus.APPROVED);

        publishEvent("Claim", claimId, "claims.claims.approved", Map.of(
                "claimId", claimId,
                "policyId", claim.getPolicyId(),
                "customerId", claim.getCustomerId(),
                "approvedAmount", approvedAmount,
                "approvedBy", approvedBy,
                "approvedAt", Instant.now().toString()
        ));
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim closeClaim(String claimId) {
        Claim claim = getByClaimId(claimId);
        claim.setStatus(ClaimStatus.CLOSED);

        publishEvent("Claim", claimId, "claims.claims.closed", Map.of(
                "claimId", claimId,
                "closedAt", Instant.now().toString()
        ));
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim flagForLitigation(String claimId) {
        Claim claim = getByClaimId(claimId);
        claim.setLitigationFlagged(true);
        claim.setStatus(ClaimStatus.LITIGATED);
        return claimRepository.save(claim);
    }

    @Transactional(readOnly = true)
    public Claim getClaim(String claimId) {
        return getByClaimId(claimId);
    }

    private Claim getByClaimId(String claimId) {
        return claimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found: " + claimId));
    }

    private void publishEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .build();
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private String generateClaimId(String lineOfBusiness) {
        String lob = lineOfBusiness.substring(0, Math.min(4, lineOfBusiness.length())).toUpperCase();
        return String.format("CLM-%s-%d-%05d", lob, Year.now().getValue(), SEQUENCE.getAndIncrement());
    }
}
