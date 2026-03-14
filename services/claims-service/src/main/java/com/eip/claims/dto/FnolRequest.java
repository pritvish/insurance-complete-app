package com.eip.claims.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FnolRequest(
        @NotBlank String policyId,
        @NotBlank String customerId,
        @NotBlank String lineOfBusiness,
        @NotNull @PastOrPresent LocalDate dateOfLoss,
        @NotBlank @Size(max = 2000) String description,
        @Positive BigDecimal estimatedAmount,
        String contactPhone
) {}
