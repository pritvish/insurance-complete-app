package com.eip.workflow.domain;

import com.eip.workflow.domain.enums.StepStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga_steps",
        indexes = {
                @Index(name = "idx_saga_step_instance", columnList = "saga_instance_id, step_order")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_instance_id", nullable = false)
    private SagaInstance sagaInstance;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_status", nullable = false, length = 50)
    @Builder.Default
    private StepStatus stepStatus = StepStatus.PENDING;

    @Column(name = "input_payload", columnDefinition = "TEXT")
    private String inputPayload;

    @Column(name = "output_payload", columnDefinition = "TEXT")
    private String outputPayload;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Version
    private Long version;
}
