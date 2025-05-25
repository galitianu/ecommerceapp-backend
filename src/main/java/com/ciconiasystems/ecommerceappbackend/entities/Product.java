package com.ciconiasystems.ecommerceappbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product extends BaseEntity {
    @Column(unique = true)
    private String slug;
    private String name;
    private String image;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn()
    @JsonIgnore
    private Category category;
    private boolean recentlyAdded;
    private boolean featured;
    private boolean hero;
    private double price;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String features;
    @OneToMany(orphanRemoval = true)
    private List<IncludedItem> includes = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_gallery_id")
    private ProductImageGallery imageGallery;

    @Column(columnDefinition = "boolean default false")
    private boolean disabled = false;

    public void addIncludedItem(IncludedItem includedItem) {
        includes.add(includedItem);
    }

}
