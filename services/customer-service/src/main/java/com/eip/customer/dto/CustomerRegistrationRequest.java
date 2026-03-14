package com.eip.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CustomerRegistrationRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email String email,
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank @Size(min = 9, max = 9) String ssn,
        @NotNull @Valid AddressDto address,
        @NotNull boolean consentGiven
) {}
