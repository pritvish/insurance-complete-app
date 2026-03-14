package com.eip.policy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only event store for the Policy aggregate (Event Sourcing).
 * Never update or delete records in this table.
 */
@Entity
@Table(name = "policy_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "event_version"}),
        indexes = @Index(name = "idx_policy_events_policy_id", columnList = "policy_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "policy_id", nullable = false, length = 50)
    private String policyId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private Long eventVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "causation_id", length = 100)
    private String causationId;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Column(name = "recorded_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant recordedAt = Instant.now();
}
