package com.ciconiasystems.ecommerceappbackend.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlaceInformation extends BaseEntity  {
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
}
