package com.eip.audit.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit record. Never update or delete.
 * DB permissions should REVOKE UPDATE, DELETE on this table.
 */
@Entity
@Table(name = "audit_records",
        indexes = {
                @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_correlation", columnList = "correlation_id"),
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_event_type", columnList = "event_type"),
                @Index(name = "idx_audit_recorded_at", columnList = "recorded_at")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "service_source", length = 50)
    private String serviceSource;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "previous_state", columnDefinition = "TEXT")
    private String previousState;

    @Column(name = "new_state", columnDefinition = "TEXT")
    private String newState;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant recordedAt = Instant.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "session_id")
    private String sessionId;
}
