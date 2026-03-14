package com.eip.analytics.repository;

import com.eip.analytics.domain.PolicyMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PolicyMetricsRepository extends JpaRepository<PolicyMetrics, UUID> {

    Optional<PolicyMetrics> findByPolicyId(UUID policyId);

    Page<PolicyMetrics> findByProductCode(String productCode, Pageable pageable);

    Page<PolicyMetrics> findByStatus(String status, Pageable pageable);

    Page<PolicyMetrics> findByIssuedAtBetween(Instant from, Instant to, Pageable pageable);

    Page<PolicyMetrics> findByStatusAndProductCode(String status, String productCode, Pageable pageable);

    Page<PolicyMetrics> findByStatusAndIssuedAtBetween(String status, Instant from, Instant to, Pageable pageable);

    Page<PolicyMetrics> findByProductCodeAndIssuedAtBetween(String productCode, Instant from, Instant to, Pageable pageable);

    Page<PolicyMetrics> findByStatusAndProductCodeAndIssuedAtBetween(
            String status, String productCode, Instant from, Instant to, Pageable pageable);
}
