package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.repositories.CartRepository;
import com.ciconiasystems.ecommerceappbackend.services.CartItemService;
import com.ciconiasystems.ecommerceappbackend.services.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemService cartItemService;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddProductNewItem() {
        Cart cart = new Cart();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        int quantity = 2;

        when(cartItemService.findByCartAndProduct(cart, product)).thenReturn(null);

        CartItem result = cartService.addProduct(cart, product, quantity);

        assertNotNull(result);
        assertEquals(quantity, result.getQuantity());
        verify(cartItemService, times(1)).save(result);
    }

    @Test
    void testAddProductDuplicateItem() {
        Cart cart = new Cart();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        int quantity = 2;

        CartItem existingCartItem = new CartItem();
        existingCartItem.setCart(cart);
        existingCartItem.setProduct(product);
        existingCartItem.setQuantity(1);

        when(cartItemService.findByCartAndProduct(cart, product)).thenReturn(existingCartItem);

        ValidationException exception = assertThrows(ValidationException.class, () -> cartService.addProduct(cart, product, quantity));
        assertEquals(ErrorCode.DUPLICATE_PRODUCT_IN_CART, exception.getErrorCode());
        verify(cartItemService, never()).save(any(CartItem.class));
    }

    @Test
    void testFindOrCreateCartExisting() {
        User user = new User();
        Cart existingCart = new Cart();
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(existingCart));

        Cart result = cartService.findOrCreateCart(user);

        assertSame(existingCart, result);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testFindOrCreateCartNew() {
        User user = new User();
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        Cart result = cartService.findOrCreateCart(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        verify(cartRepository, times(1)).save(result);
    }
}
