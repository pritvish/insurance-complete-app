package com.eip.audit.service;

import com.eip.audit.domain.AuditRecord;
import com.eip.audit.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;

    /**
     * Record an audit event. ONLY saves - never updates existing records.
     * The audit log is immutable by design.
     */
    @Transactional
    public AuditRecord recordEvent(AuditRecord record) {
        AuditRecord saved = auditRecordRepository.save(record);
        log.debug("Audit record saved: id={}, eventType={}, entityId={}",
                saved.getId(), saved.getEventType(), saved.getEntityId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AuditRecord> getAuditHistory(String entityType, String entityId, Pageable pageable) {
        return auditRecordRepository.findByEntityTypeAndEntityIdOrderByRecordedAtDesc(
                entityType, entityId, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> getByCorrelationId(String correlationId) {
        return auditRecordRepository.findByCorrelationId(correlationId);
    }
}
