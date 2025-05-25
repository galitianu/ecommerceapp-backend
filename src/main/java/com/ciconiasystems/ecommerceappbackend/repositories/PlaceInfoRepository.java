package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.PlaceInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceInfoRepository extends JpaRepository<PlaceInformation, Long> {
    PlaceInformation findByAddress(String address);
}
