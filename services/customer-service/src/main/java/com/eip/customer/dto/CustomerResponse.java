package com.eip.customer.dto;

import com.eip.customer.domain.CustomerStatus;
import com.eip.customer.domain.KycStatus;
import com.eip.customer.domain.OfacStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String customerId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        CustomerStatus status,
        KycStatus kycStatus,
        Instant kycCompletedAt,
        OfacStatus ofacScreeningStatus,
        Integer creditScore,
        Boolean consentGiven,
        AddressDto address,
        Instant createdAt,
        Instant updatedAt
) {}
