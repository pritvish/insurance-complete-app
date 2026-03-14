package com.eip.claims.dto;

import jakarta.validation.constraints.NotBlank;

public record ClaimAssignmentRequest(
        @NotBlank String claimId,
        @NotBlank String adjusterId,
        String assignmentReason
) {}
