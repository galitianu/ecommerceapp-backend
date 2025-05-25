package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
}