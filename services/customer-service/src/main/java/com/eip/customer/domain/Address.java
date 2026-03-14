package com.eip.customer.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zipCode;

    @Builder.Default
    private String country = "US";
}
