package com.ciconiasystems.ecommerceappbackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Category extends BaseEntity {
    private String name;
    private String image;
    @Column(unique = true)
    private String slug;
    @Column(columnDefinition = "boolean default false")
    private boolean disabled = false;
}
