package com.eip.referencedata.repository;

import com.eip.referencedata.domain.StateRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRuleRepository extends JpaRepository<StateRule, UUID> {
    Optional<StateRule> findByStateCodeAndLineOfBusiness(String stateCode, String lineOfBusiness);
    List<StateRule> findByStateCode(String stateCode);
    List<StateRule> findByIsActiveTrue();
}
