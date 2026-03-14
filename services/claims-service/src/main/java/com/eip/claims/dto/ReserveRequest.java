package com.eip.claims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ReserveRequest(
        @NotBlank String claimId,
        @NotNull @Positive BigDecimal reserveAmount,
        @NotBlank String justification
) {}
