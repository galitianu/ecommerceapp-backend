package com.ciconiasystems.ecommerceappbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "\"order\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {
    @ManyToOne
    private Person person;

    private ZonedDateTime datePlaced;

    private double total;

    @OneToOne
    private BillingInformation billingInformation;

    @Column(columnDefinition = "boolean default false")
    private boolean delivered = false;

    @Column(columnDefinition = "boolean default true")
    private boolean pending = true;

    private String stripeClientSecret;

    @ManyToOne
    private DeliveryPerson deliveryPerson = null;
}