package com.eip.document.repository;

import com.eip.document.domain.DocumentRecord;
import com.eip.document.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, UUID> {
    Optional<DocumentRecord> findByDocumentId(String documentId);
    List<DocumentRecord> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<DocumentRecord> findByCustomerId(String customerId);
    List<DocumentRecord> findByCustomerIdAndDocumentType(String customerId, DocumentType type);
}
