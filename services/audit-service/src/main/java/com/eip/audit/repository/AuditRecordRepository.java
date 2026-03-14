package com.eip.audit.repository;

import com.eip.audit.domain.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, UUID> {

    Page<AuditRecord> findByEntityTypeAndEntityIdOrderByRecordedAtDesc(
            String entityType, String entityId, Pageable pageable);

    List<AuditRecord> findByCorrelationId(String correlationId);

    Page<AuditRecord> findByUserIdAndOccurredAtBetween(
            String userId, Instant from, Instant to, Pageable pageable);

    Page<AuditRecord> findByEventTypeOrderByRecordedAtDesc(String eventType, Pageable pageable);
}
