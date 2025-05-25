package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    private final CartItemService cartItemService;
    public CartItem addProduct(Cart cart, Product product, int quantity) {
        CartItem existingCartItem = findExistingCartItem(cart, product);

        if (quantity <= 0) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }

        if (existingCartItem == null) {
            CartItem newCartItem = createCartItem(cart, product, quantity);
            cartItemService.save(newCartItem);
            return newCartItem;
        } else {
            throw new ValidationException(ErrorCode.DUPLICATE_PRODUCT_IN_CART);
        }
    }

    private CartItem createCartItem(Cart cart, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }


    private CartItem findExistingCartItem(Cart cart, Product product) {
        return cartItemService.findByCartAndProduct(cart, product);
    }

    public Cart findOrCreateCart(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);

        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            Cart newCart = new Cart();
            newCart.setUser(user);
            cartRepository.save(newCart);

            return newCart;
        }
    }

    public Cart findCart(User user) {
        return cartRepository.findByUser(user).orElse(null);
    }

    public void removeAllItemsFromCart(User user) {
        Cart cart = findCart(user);
        if (cart != null) {
            cartItemService.deleteByCart(cart);
        }
    }

    public List<CartItem> getCartItems(Cart cart) {
        return cartItemService.findByCartOrderByIdAsc(cart);
    }

    public CartItem addQuantityToProduct(Cart cart, Product product, int quantity) {
        CartItem existingCartItem = findExistingCartItem(cart, product);

        if (existingCartItem == null) {
            if (quantity <= 0) {
                throw new ValidationException(ErrorCode.INVALID_QUANTITY);
            }
            CartItem newCartItem = createCartItem(cart, product, quantity);
            cartItemService.save(newCartItem);
            return newCartItem;
        } else {
            if (existingCartItem.getQuantity() == 1 && quantity == -1) {
                cartItemService.delete(existingCartItem);
                return null;
            }
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            cartItemService.save(existingCartItem);
            return existingCartItem;
        }
    }


    public Optional<CartItem> findCartItemById(UUID cartItemId) {
        return cartItemService.findById(cartItemId);
    }

    public void removeCartItem(CartItem cartItem) {
        cartItemService.delete(cartItem);
    }

    public boolean checkIfCartItemBelongsToCart(Cart cart, CartItem cartItem) {
        return cartItem.getCart().equals(cart);
    }

    public boolean checkQuantityNotGreaterThanZero(int quantity) {
        return quantity <= 0;
    }

    public void deleteDisabledCartItems(Cart cart) {
        List<CartItem> cartItems = cartItemService.findByCartOrderByIdAsc(cart);
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct().isDisabled()) {
                removeCartItem(cartItem);
            }
        }
    }
}
