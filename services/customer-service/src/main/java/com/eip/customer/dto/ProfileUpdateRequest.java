package com.eip.customer.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 20) @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone,
        AddressDto address
) {}
