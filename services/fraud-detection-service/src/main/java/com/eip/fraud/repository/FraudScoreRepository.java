package com.eip.fraud.repository;

import com.eip.fraud.domain.FraudScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FraudScoreRepository extends JpaRepository<FraudScore, UUID> {
    Optional<FraudScore> findByClaimId(String claimId);
}
