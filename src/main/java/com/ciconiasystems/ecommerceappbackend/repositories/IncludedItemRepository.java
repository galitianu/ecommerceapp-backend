package com.ciconiasystems.ecommerceappbackend.repositories;


import com.ciconiasystems.ecommerceappbackend.entities.IncludedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncludedItemRepository extends JpaRepository<IncludedItem, Long> {
}
