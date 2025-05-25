package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);

    List<CartItem> findByCartOrderByIdAsc(Cart cart);
}
