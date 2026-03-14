package com.eip.customer.dto;

public record AddressDto(
        String street1,
        String street2,
        String city,
        String state,
        String zipCode,
        String country
) {}
