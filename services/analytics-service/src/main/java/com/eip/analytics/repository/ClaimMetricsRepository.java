package com.eip.analytics.repository;

import com.eip.analytics.domain.ClaimMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ClaimMetricsRepository extends JpaRepository<ClaimMetrics, UUID> {

    Optional<ClaimMetrics> findByClaimId(UUID claimId);

    Page<ClaimMetrics> findByPolicyId(UUID policyId, Pageable pageable);

    Page<ClaimMetrics> findByStatus(String status, Pageable pageable);

    Page<ClaimMetrics> findByFiledAtBetween(Instant from, Instant to, Pageable pageable);

    Page<ClaimMetrics> findByStatusAndFiledAtBetween(String status, Instant from, Instant to, Pageable pageable);
}
