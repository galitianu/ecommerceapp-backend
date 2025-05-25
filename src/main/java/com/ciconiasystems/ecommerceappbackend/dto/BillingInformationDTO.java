package com.ciconiasystems.ecommerceappbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingInformationDTO {
    @NotNull
    private String phoneNumber;
    @NotNull
    private String address;
    @NotNull
    private String zipCode;
    @NotNull
    private String city;
    @NotNull
    private String country;
    @NotNull
    private String paymentOption;
}
