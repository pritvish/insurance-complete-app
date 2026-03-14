package com.eip.policy.service;

import com.eip.policy.client.PremiumCalcClient;
import com.eip.policy.domain.*;
import com.eip.policy.dto.*;
import com.eip.policy.exception.PolicyNotFoundException;
import com.eip.policy.exception.InvalidPolicyStateException;
import com.eip.policy.repository.PolicyProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyCommandService {

    private final PolicyEventStore eventStore;
    private final PolicyProjectionRepository projectionRepository;
    private final PremiumCalcClient premiumCalcClient;

    private static final AtomicLong SEQUENCE = new AtomicLong(1000);

    @Transactional
    public QuoteResponse createQuote(QuoteRequest request, String correlationId) {
        String quoteId = "QUO-" + Year.now().getValue() + "-" + SEQUENCE.getAndIncrement();
        String policyId = "POL-" + Year.now().getValue() + "-" + SEQUENCE.getAndIncrement();

        // Call Premium Calc service
        PremiumCalcClient.PremiumCalculationResult calcResult = premiumCalcClient.calculate(
                new PremiumCalcClient.PremiumCalculationRequest(
                        request.customerId(),
                        request.lineOfBusiness().name(),
                        request.productCode(),
                        request.stateCode(),
                        request.coverageLimit(),
                        request.deductible(),
                        request.effectiveDate(),
                        request.expirationDate(),
                        Map.of()
                )
        );

        // Append QUOTED event to event store
        eventStore.appendEvent(policyId, "policy.policies.quoted", Map.of(
                "quoteId", quoteId,
                "policyId", policyId,
                "customerId", request.customerId(),
                "brokerId", request.brokerId() != null ? request.brokerId() : "",
                "lineOfBusiness", request.lineOfBusiness().name(),
                "productCode", request.productCode(),
                "stateCode", request.stateCode(),
                "premiumAmount", calcResult.finalPremium(),
                "coverageLimit", request.coverageLimit(),
                "deductible", request.deductible(),
                "effectiveDate", request.effectiveDate().toString(),
                "expirationDate", request.expirationDate().toString(),
                "quotedAt", Instant.now().toString()
        ), correlationId);

        // Create projection
        PolicyProjection projection = PolicyProjection.builder()
                .policyId(policyId)
                .quoteId(quoteId)
                .customerId(request.customerId())
                .brokerId(request.brokerId())
                .lineOfBusiness(request.lineOfBusiness())
                .productCode(request.productCode())
                .status(PolicyStatus.QUOTED)
                .effectiveDate(request.effectiveDate())
                .expirationDate(request.expirationDate())
                .premiumAmount(calcResult.finalPremium())
                .coverageLimit(request.coverageLimit())
                .deductible(request.deductible())
                .stateCode(request.stateCode())
                .insuredName(request.insuredName())
                .build();
        projectionRepository.save(projection);

        return new QuoteResponse(
                quoteId, policyId, calcResult.finalPremium(),
                request.coverageLimit(), request.deductible(),
                request.effectiveDate(), request.expirationDate(),
                Instant.now().plusSeconds(1800), // 30-min quote validity
                request.lineOfBusiness().name(), request.stateCode()
        );
    }

    @Transactional
    public PolicyProjection bindPolicy(BindRequest request, String correlationId) {
        PolicyProjection projection = projectionRepository.findByQuoteId(request.quoteId())
                .orElseThrow(() -> new PolicyNotFoundException("Quote not found: " + request.quoteId()));

        if (projection.getStatus() != PolicyStatus.QUOTED) {
            throw new InvalidPolicyStateException("Policy cannot be bound in status: " + projection.getStatus());
        }

        eventStore.appendEvent(projection.getPolicyId(), "policy.policies.bound", Map.of(
                "policyId", projection.getPolicyId(),
                "quoteId", request.quoteId(),
                "paymentMethodToken", request.paymentMethodToken(),
                "boundAt", Instant.now().toString()
        ), correlationId);

        projection.setStatus(PolicyStatus.BOUND);
        projection.setUpdatedAt(Instant.now());
        return projectionRepository.save(projection);
    }

    @Transactional
    public PolicyProjection issuePolicy(String policyId, String correlationId) {
        PolicyProjection projection = projectionRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found: " + policyId));

        if (projection.getStatus() != PolicyStatus.BOUND) {
            throw new InvalidPolicyStateException("Policy must be BOUND to issue. Current: " + projection.getStatus());
        }

        eventStore.appendEvent(policyId, "policy.policies.issued", Map.of(
                "policyId", policyId,
                "customerId", projection.getCustomerId(),
                "brokerId", projection.getBrokerId() != null ? projection.getBrokerId() : "",
                "lineOfBusiness", projection.getLineOfBusiness().name(),
                "premiumAmount", projection.getPremiumAmount(),
                "effectiveDate", projection.getEffectiveDate().toString(),
                "issuedAt", Instant.now().toString()
        ), correlationId);

        projection.setStatus(PolicyStatus.ISSUED);
        projection.setUpdatedAt(Instant.now());
        return projectionRepository.save(projection);
    }

    @Transactional
    public PolicyProjection cancelPolicy(String policyId, String reason, String correlationId) {
        PolicyProjection projection = projectionRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found: " + policyId));

        if (projection.getStatus() == PolicyStatus.CANCELLED) {
            throw new InvalidPolicyStateException("Policy is already cancelled");
        }

        eventStore.appendEvent(policyId, "policy.policies.cancelled", Map.of(
                "policyId", policyId,
                "customerId", projection.getCustomerId(),
                "reason", reason,
                "cancelledAt", Instant.now().toString()
        ), correlationId);

        projection.setStatus(PolicyStatus.CANCELLED);
        projection.setUpdatedAt(Instant.now());
        return projectionRepository.save(projection);
    }

    @Transactional(readOnly = true)
    public PolicyProjection getPolicy(String policyId) {
        return projectionRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found: " + policyId));
    }
}
