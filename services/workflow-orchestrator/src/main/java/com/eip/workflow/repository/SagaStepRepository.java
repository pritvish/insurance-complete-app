package com.eip.workflow.repository;

import com.eip.workflow.domain.SagaInstance;
import com.eip.workflow.domain.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaStepRepository extends JpaRepository<SagaStep, UUID> {

    List<SagaStep> findBySagaInstanceOrderByStepOrderAsc(SagaInstance sagaInstance);

    Optional<SagaStep> findBySagaInstanceAndStepName(SagaInstance sagaInstance, String stepName);
}
