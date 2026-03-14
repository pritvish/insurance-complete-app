package com.eip.document.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_doc_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_doc_customer", columnList = "customer_id"),
        @Index(name = "idx_doc_type", columnList = "document_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false, unique = true)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "gcs_bucket")
    private String gcsBucket;

    @Column(name = "gcs_object_key")
    private String gcsObjectKey;

    @Column(name = "gcs_signed_url_expiry")
    private Instant gcsSignedUrlExpiry;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "signed_by")
    private String signedBy;

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private Instant generatedAt;
}
