package com.eip.workflow.repository;

import com.eip.workflow.domain.SagaInstance;
import com.eip.workflow.domain.enums.SagaStatus;
import com.eip.workflow.domain.enums.SagaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, UUID> {

    Optional<SagaInstance> findByCorrelationIdAndSagaType(String correlationId, SagaType sagaType);

    List<SagaInstance> findByCorrelationId(String correlationId);

    Page<SagaInstance> findBySagaStatus(SagaStatus sagaStatus, Pageable pageable);
}
