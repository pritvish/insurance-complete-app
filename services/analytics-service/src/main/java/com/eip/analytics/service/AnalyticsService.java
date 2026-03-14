package com.eip.analytics.service;

import com.eip.analytics.domain.BrokerMetrics;
import com.eip.analytics.domain.ClaimMetrics;
import com.eip.analytics.domain.PolicyMetrics;
import com.eip.analytics.repository.BrokerMetricsRepository;
import com.eip.analytics.repository.ClaimMetricsRepository;
import com.eip.analytics.repository.PolicyMetricsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsService {

    private final PolicyMetricsRepository policyMetricsRepository;
    private final ClaimMetricsRepository claimMetricsRepository;
    private final BrokerMetricsRepository brokerMetricsRepository;

    public void upsertPolicyMetrics(JsonNode event, String eventType) {
        String policyIdStr = event.path("policyId").asText(null);
        if (policyIdStr == null || policyIdStr.isBlank()) {
            log.warn("upsertPolicyMetrics: missing policyId in event type={}", eventType);
            return;
        }
        UUID policyId = UUID.fromString(policyIdStr);

        PolicyMetrics metrics = policyMetricsRepository.findByPolicyId(policyId)
                .orElseGet(() -> PolicyMetrics.builder().policyId(policyId).build());

        // Update fields present in this event
        if (event.has("customerId")) metrics.setCustomerId(UUID.fromString(event.path("customerId").asText()));
        if (event.has("brokerId")) metrics.setBrokerId(UUID.fromString(event.path("brokerId").asText()));
        if (event.has("productCode")) metrics.setProductCode(event.path("productCode").asText());
        if (event.has("status")) metrics.setStatus(event.path("status").asText());
        if (event.has("premiumAmount")) metrics.setPremiumAmount(new BigDecimal(event.path("premiumAmount").asText()));
        if (event.has("coverageAmount")) metrics.setCoverageAmount(new BigDecimal(event.path("coverageAmount").asText()));
        if (event.has("issuedAt")) metrics.setIssuedAt(Instant.parse(event.path("issuedAt").asText()));
        if (event.has("expiresAt")) metrics.setExpiresAt(Instant.parse(event.path("expiresAt").asText()));

        metrics.setLastEventType(eventType);
        metrics.setLastUpdatedAt(Instant.now());
        policyMetricsRepository.save(metrics);
    }

    public void upsertClaimMetrics(JsonNode event, String eventType) {
        String claimIdStr = event.path("claimId").asText(null);
        if (claimIdStr == null || claimIdStr.isBlank()) {
            log.warn("upsertClaimMetrics: missing claimId in event type={}", eventType);
            return;
        }
        UUID claimId = UUID.fromString(claimIdStr);

        ClaimMetrics metrics = claimMetricsRepository.findByClaimId(claimId)
                .orElseGet(() -> ClaimMetrics.builder().claimId(claimId).build());

        if (event.has("policyId")) metrics.setPolicyId(UUID.fromString(event.path("policyId").asText()));
        if (event.has("customerId")) metrics.setCustomerId(UUID.fromString(event.path("customerId").asText()));
        if (event.has("claimType")) metrics.setClaimType(event.path("claimType").asText());
        if (event.has("status")) metrics.setStatus(event.path("status").asText());
        if (event.has("filedAmount")) metrics.setFiledAmount(new BigDecimal(event.path("filedAmount").asText()));
        if (event.has("approvedAmount")) metrics.setApprovedAmount(new BigDecimal(event.path("approvedAmount").asText()));
        if (event.has("filedAt")) metrics.setFiledAt(Instant.parse(event.path("filedAt").asText()));
        if (event.has("settledAt")) {
            Instant settledAt = Instant.parse(event.path("settledAt").asText());
            metrics.setSettledAt(settledAt);
            if (metrics.getFiledAt() != null) {
                long days = java.time.Duration.between(metrics.getFiledAt(), settledAt).toDays();
                metrics.setProcessingDaysCount((int) days);
            }
        }

        // Increment policy's claim count when a new claim is filed
        if ("claims.claims.filed".equals(eventType) && metrics.getPolicyId() != null) {
            policyMetricsRepository.findByPolicyId(metrics.getPolicyId()).ifPresent(pm -> {
                pm.setClaimCount(pm.getClaimCount() + 1);
                policyMetricsRepository.save(pm);
            });
        }

        metrics.setLastEventType(eventType);
        metrics.setLastUpdatedAt(Instant.now());
        claimMetricsRepository.save(metrics);
    }

    public void updateFraudScore(JsonNode event) {
        String claimIdStr = event.path("claimId").asText(null);
        if (claimIdStr == null || claimIdStr.isBlank()) return;
        UUID claimId = UUID.fromString(claimIdStr);

        claimMetricsRepository.findByClaimId(claimId).ifPresent(metrics -> {
            if (event.has("fraudScore")) {
                metrics.setFraudScore(new BigDecimal(event.path("fraudScore").asText()));
            }
            if (event.has("isFraudulent")) {
                metrics.setIsFraudulent(event.path("isFraudulent").asBoolean());
            }
            metrics.setLastEventType("fraud.scores.returned");
            metrics.setLastUpdatedAt(Instant.now());
            claimMetricsRepository.save(metrics);
        });
    }

    public void upsertBrokerMetrics(JsonNode event, String eventType) {
        String brokerIdStr = event.path("brokerId").asText(null);
        if (brokerIdStr == null || brokerIdStr.isBlank()) return;
        UUID brokerId = UUID.fromString(brokerIdStr);

        BrokerMetrics metrics = brokerMetricsRepository.findByBrokerId(brokerId)
                .orElseGet(() -> BrokerMetrics.builder().brokerId(brokerId).build());

        if (event.has("brokerName")) metrics.setBrokerName(event.path("brokerName").asText());

        switch (eventType) {
            case "policy.policies.quoted" -> metrics.setTotalPolicies(metrics.getTotalPolicies() + 1);
            case "policy.policies.issued" -> {
                metrics.setActivePolicies(metrics.getActivePolicies() + 1);
                if (event.has("premiumAmount")) {
                    BigDecimal premium = new BigDecimal(event.path("premiumAmount").asText());
                    metrics.setTotalPremiumCollected(metrics.getTotalPremiumCollected().add(premium));
                }
            }
            case "policy.policies.cancelled" -> metrics.setActivePolicies(Math.max(0, metrics.getActivePolicies() - 1));
            case "claims.claims.filed" -> {
                metrics.setTotalClaimsCount(metrics.getTotalClaimsCount() + 1);
                if (metrics.getTotalPolicies() > 0) {
                    metrics.setClaimsRatio(
                            BigDecimal.valueOf(metrics.getTotalClaimsCount())
                                      .divide(BigDecimal.valueOf(metrics.getTotalPolicies()), 4, RoundingMode.HALF_UP));
                }
            }
        }

        metrics.setLastEventType(eventType);
        metrics.setLastUpdatedAt(Instant.now());
        brokerMetricsRepository.save(metrics);
    }

    @Transactional(readOnly = true)
    public Page<PolicyMetrics> getPolicySummary(String status, String productCode, Instant from, Instant to, Pageable pageable) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasProduct = productCode != null && !productCode.isBlank();
        boolean hasRange = from != null && to != null;

        if (hasStatus && hasProduct && hasRange) {
            return policyMetricsRepository.findByStatusAndProductCodeAndIssuedAtBetween(status, productCode, from, to, pageable);
        } else if (hasStatus && hasProduct) {
            return policyMetricsRepository.findByStatusAndProductCode(status, productCode, pageable);
        } else if (hasStatus && hasRange) {
            return policyMetricsRepository.findByStatusAndIssuedAtBetween(status, from, to, pageable);
        } else if (hasProduct && hasRange) {
            return policyMetricsRepository.findByProductCodeAndIssuedAtBetween(productCode, from, to, pageable);
        } else if (hasStatus) {
            return policyMetricsRepository.findByStatus(status, pageable);
        } else if (hasProduct) {
            return policyMetricsRepository.findByProductCode(productCode, pageable);
        } else if (hasRange) {
            return policyMetricsRepository.findByIssuedAtBetween(from, to, pageable);
        } else {
            return policyMetricsRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public PolicyMetrics getPolicyMetrics(UUID policyId) {
        return policyMetricsRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "PolicyMetrics not found for policyId=" + policyId));
    }

    @Transactional(readOnly = true)
    public Page<ClaimMetrics> getClaimSummary(String status, Instant from, Instant to, Pageable pageable) {
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasRange = from != null && to != null;

        if (hasStatus && hasRange) {
            return claimMetricsRepository.findByStatusAndFiledAtBetween(status, from, to, pageable);
        } else if (hasStatus) {
            return claimMetricsRepository.findByStatus(status, pageable);
        } else if (hasRange) {
            return claimMetricsRepository.findByFiledAtBetween(from, to, pageable);
        } else {
            return claimMetricsRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public ClaimMetrics getClaimMetrics(UUID claimId) {
        return claimMetricsRepository.findByClaimId(claimId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "ClaimMetrics not found for claimId=" + claimId));
    }

    @Transactional(readOnly = true)
    public Page<BrokerMetrics> getBrokerLeaderboard(Pageable pageable) {
        return brokerMetricsRepository.findAllByOrderByTotalPremiumCollectedDesc(pageable);
    }

    @Transactional(readOnly = true)
    public BrokerMetrics getBrokerMetrics(UUID brokerId) {
        return brokerMetricsRepository.findByBrokerId(brokerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "BrokerMetrics not found for brokerId=" + brokerId));
    }
}
