package com.eip.policy.repository;

import com.eip.policy.domain.PolicyProjection;
import com.eip.policy.domain.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyProjectionRepository extends JpaRepository<PolicyProjection, UUID> {

    Optional<PolicyProjection> findByPolicyId(String policyId);

    Page<PolicyProjection> findByCustomerId(String customerId, Pageable pageable);

    Page<PolicyProjection> findByBrokerId(String brokerId, Pageable pageable);

    Page<PolicyProjection> findByStatus(PolicyStatus status, Pageable pageable);

    Page<PolicyProjection> findByStatusAndExpirationDateBefore(PolicyStatus status, LocalDate date, Pageable pageable);

    Optional<PolicyProjection> findByQuoteId(String quoteId);
}
