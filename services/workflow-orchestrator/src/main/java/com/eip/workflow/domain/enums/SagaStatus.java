package com.eip.workflow.domain.enums;

public enum SagaStatus {
    STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
