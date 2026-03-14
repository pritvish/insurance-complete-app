package com.eip.workflow.domain;

import com.eip.workflow.domain.enums.SagaStatus;
import com.eip.workflow.domain.enums.SagaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "saga_instances",
        indexes = {
                @Index(name = "idx_saga_correlation", columnList = "correlation_id"),
                @Index(name = "idx_saga_status", columnList = "saga_status"),
                @Index(name = "idx_saga_correlation_type", columnList = "correlation_id, saga_type", unique = true)
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_type", nullable = false, length = 50)
    private SagaType sagaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_status", nullable = false, length = 50)
    private SagaStatus sagaStatus;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "sagaInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<SagaStep> steps = new ArrayList<>();
}
