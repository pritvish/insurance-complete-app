package com.eip.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public record EndorsementRequest(
        @NotBlank String policyId,
        @NotBlank String endorsementType,
        @NotNull Map<String, Object> changes,
        @NotNull LocalDate effectiveDate,
        String reasonCode
) {}
