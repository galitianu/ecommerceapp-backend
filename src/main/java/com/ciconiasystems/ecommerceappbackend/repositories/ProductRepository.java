package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);
    List<Product> findByDisabledTrue();
    Optional<Product> findByIdAndDisabledFalse(UUID productId);

    List<Product> findByCategorySlugAndDisabledFalse(String categorySlug);

    Optional<Object> findByIdAndDisabledTrue(UUID productId);

    boolean existsBySlug(String uniqueSlug);

    List<Product> findByName(String name);
}
