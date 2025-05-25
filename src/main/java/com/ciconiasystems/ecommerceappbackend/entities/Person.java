package com.ciconiasystems.ecommerceappbackend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
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
public class Person extends BaseEntity {
    @OneToOne
    private User user;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
}