package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String categorySlug);

    List<Category> findByDisabledFalse();

    List<Category> findByDisabledTrue();

    boolean existsBySlugAndDisabledFalse(String categorySlug);

    Optional<Category> findByName(String name);
}
