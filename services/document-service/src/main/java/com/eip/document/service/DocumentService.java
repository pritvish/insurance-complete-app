package com.eip.document.service;

import com.eip.document.domain.DocumentRecord;
import com.eip.document.domain.DocumentType;
import com.eip.document.repository.DocumentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRecordRepository documentRecordRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final GcsStorageService gcsStorageService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final AtomicLong DOC_SEQ = new AtomicLong(1000);

    @Transactional
    public DocumentRecord generateDocument(DocumentType documentType, String entityType,
                                            String entityId, String customerId,
                                            String correlationId, Map<String, String> data) {
        String docId = "DOC-" + java.time.Year.now().getValue() + "-" + DOC_SEQ.getAndIncrement();

        byte[] pdfBytes = pdfGeneratorService.generatePdf(documentType, data);

        String objectKey = String.format("documents/%s/%s/%s.pdf", entityType.toLowerCase(), entityId, docId);
        gcsStorageService.uploadDocument(objectKey, pdfBytes, "application/pdf");

        DocumentRecord record = DocumentRecord.builder()
                .documentId(docId)
                .documentType(documentType)
                .entityType(entityType)
                .entityId(entityId)
                .customerId(customerId)
                .gcsBucket(gcsStorageService.getBucketName())
                .gcsObjectKey(objectKey)
                .fileSizeBytes((long) pdfBytes.length)
                .contentType("application/pdf")
                .correlationId(correlationId)
                .build();

        record = documentRecordRepository.save(record);

        // Publish document generated event
        String payload = String.format(
                "{\"documentId\":\"%s\",\"documentType\":\"%s\",\"entityType\":\"%s\",\"entityId\":\"%s\",\"customerId\":\"%s\"}",
                docId, documentType, entityType, entityId, customerId);
        kafkaTemplate.send("document.documents.generated", docId, payload);

        log.info("Document generated: {} type={} entityId={}", docId, documentType, entityId);
        return record;
    }

    @Transactional
    public DocumentRecord markSigned(String documentId, String signedBy) {
        DocumentRecord record = documentRecordRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        record.setSignedAt(Instant.now());
        record.setSignedBy(signedBy);
        record = documentRecordRepository.save(record);

        String payload = String.format(
                "{\"documentId\":\"%s\",\"signedBy\":\"%s\",\"signedAt\":\"%s\"}",
                documentId, signedBy, record.getSignedAt());
        kafkaTemplate.send("document.documents.signed", documentId, payload);

        return record;
    }

    public String getSignedDownloadUrl(String documentId) {
        DocumentRecord record = documentRecordRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        return gcsStorageService.generateSignedUrl(record.getGcsObjectKey(), 60);
    }

    @Transactional(readOnly = true)
    public List<DocumentRecord> getDocumentsForEntity(String entityType, String entityId) {
        return documentRecordRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<DocumentRecord> getDocumentsForCustomer(String customerId) {
        return documentRecordRepository.findByCustomerId(customerId);
    }
}
