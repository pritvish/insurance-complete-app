package com.eip.customer.dto;

import com.eip.customer.domain.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record KycCallbackRequest(
        @NotBlank String customerId,
        @NotNull KycStatus status,
        @NotBlank String referenceId,
        @NotNull Instant completedAt,
        String failureReason
) {}
