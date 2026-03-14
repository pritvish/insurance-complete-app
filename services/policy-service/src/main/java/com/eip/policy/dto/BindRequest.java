package com.eip.policy.dto;

import jakarta.validation.constraints.NotBlank;

public record BindRequest(
        @NotBlank String quoteId,
        @NotBlank String paymentMethodToken,
        String signatureReference
) {}
