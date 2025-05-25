package com.ciconiasystems.ecommerceappbackend.entities;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IncludedItem extends BaseEntity {
    private int quantity;
    private String item;
}
