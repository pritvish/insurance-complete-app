package com.eip.payment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PremiumPaymentRequest(
        @NotBlank String idempotencyKey,
        @NotBlank String policyId,
        @NotBlank String customerId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String paymentMethodToken,
        @NotBlank String paymentMethod
) {}
