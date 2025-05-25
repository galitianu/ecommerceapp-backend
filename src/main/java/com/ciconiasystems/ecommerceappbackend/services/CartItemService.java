package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartItemRepository cartItemRepository;

    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    public CartItem findByCartAndProduct(Cart cart, Product product) {
        return cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
    }

    public void deleteByCart(Cart cart) {
        cartItemRepository.deleteByCart(cart);
    }

    public Optional<CartItem> findById(UUID cartItemId) {
        return cartItemRepository.findById(cartItemId);
    }

    public void delete(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    public List<CartItem> findByCartOrderByIdAsc(Cart cart) {
        return cartItemRepository.findByCartOrderByIdAsc(cart);
    }
}
