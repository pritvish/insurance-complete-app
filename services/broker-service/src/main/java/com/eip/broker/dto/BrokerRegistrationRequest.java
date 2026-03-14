package com.eip.broker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BrokerRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone,
        String agencyName,
        @NotBlank String licenseNumber,
        @NotBlank String licenseState,
        @NotNull LocalDate licenseExpiryDate,
        BigDecimal commissionRate
) {}
