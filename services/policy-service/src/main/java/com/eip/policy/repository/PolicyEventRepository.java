package com.eip.policy.repository;

import com.eip.policy.domain.PolicyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyEventRepository extends JpaRepository<PolicyEvent, UUID> {

    List<PolicyEvent> findByPolicyIdOrderByEventVersionAsc(String policyId);

    Optional<PolicyEvent> findTopByPolicyIdOrderByEventVersionDesc(String policyId);

    @Query("SELECT COUNT(e) FROM PolicyEvent e WHERE e.policyId = :policyId")
    long countByPolicyId(String policyId);
}
