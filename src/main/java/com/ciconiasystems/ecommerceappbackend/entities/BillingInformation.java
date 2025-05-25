package com.ciconiasystems.ecommerceappbackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BillingInformation extends BaseEntity {

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
    @Enumerated(EnumType.STRING)
    private PaymentOption paymentOption;
    @ManyToOne
    private PlaceInformation placeInformation = null;
}


