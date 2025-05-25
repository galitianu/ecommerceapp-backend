package com.ciconiasystems.ecommerceappbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDTO {
    private String slug;
    private String name;
    private String image;
    private String category;
    @JsonProperty("new")
    private boolean isNew;
    private double price;
    private String description;
    private String features;
    private List<Include> includes;
    private boolean hero;
    private boolean featured;
    private String imageGallery1;
    private String imageGallery2;
    private String imageGallery3;

    @Getter
    @Setter
    public static class Include {
        private Integer quantity;
        private String item;
    }

}
