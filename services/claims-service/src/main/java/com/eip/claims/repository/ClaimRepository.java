package com.eip.claims.repository;

import com.eip.claims.domain.Claim;
import com.eip.claims.domain.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Optional<Claim> findByClaimId(String claimId);
    Page<Claim> findByPolicyId(String policyId, Pageable pageable);
    Page<Claim> findByCustomerId(String customerId, Pageable pageable);
    Page<Claim> findByAssignedAdjusterIdAndStatus(String adjusterId, ClaimStatus status, Pageable pageable);
    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);
}
