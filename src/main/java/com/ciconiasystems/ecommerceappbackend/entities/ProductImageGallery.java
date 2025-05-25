package com.ciconiasystems.ecommerceappbackend.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductImageGallery  extends BaseEntity {
    @Column(nullable = false)
    private String imageGallery1;

    @Column(nullable = false)
    private String imageGallery2;

    @Column(nullable = false)
    private String imageGallery3;
}