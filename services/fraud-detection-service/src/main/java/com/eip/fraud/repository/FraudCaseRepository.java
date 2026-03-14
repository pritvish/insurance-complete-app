package com.eip.fraud.repository;

import com.eip.fraud.domain.FraudCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FraudCaseRepository extends JpaRepository<FraudCase, UUID> {
    Optional<FraudCase> findByCaseId(String caseId);
}
