package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ciconiasystems.ecommerceappbackend.services.CartItemService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        CartItem cartItem = new CartItem();
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItem result = cartItemService.save(cartItem);

        assertNotNull(result);
        assertEquals(cartItem, result);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    void testFindByCartAndProduct() {
        Cart cart = new Cart();
        Product product = new Product();
        product.setId(UUID.randomUUID());

        CartItem expectedCartItem = new CartItem();
        expectedCartItem.setCart(cart);
        expectedCartItem.setProduct(product);

        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(expectedCartItem));

        CartItem result = cartItemService.findByCartAndProduct(cart, product);

        assertNotNull(result);
        assertEquals(expectedCartItem, result);
    }

    @Test
    void testFindByCartAndProductNotFound() {
        Cart cart = new Cart();
        Product product = new Product();
        product.setId(UUID.randomUUID());

        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        CartItem result = cartItemService.findByCartAndProduct(cart, product);

        assertNull(result);
    }

    @Test
    void testDeleteByCart() {
        Cart cart = new Cart();
        doNothing().when(cartItemRepository).deleteByCart(cart);

        cartItemService.deleteByCart(cart);

        verify(cartItemRepository, times(1)).deleteByCart(cart);
    }

    @Test
    void testFindById() {
        UUID cartItemId = UUID.randomUUID();
        CartItem expectedCartItem = new CartItem();
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(expectedCartItem));

        Optional<CartItem> result = cartItemService.findById(cartItemId);

        assertTrue(result.isPresent());
        assertEquals(expectedCartItem, result.get());
    }

    @Test
    void testFindByIdNotFound() {
        UUID cartItemId = UUID.randomUUID();
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        Optional<CartItem> result = cartItemService.findById(cartItemId);

        assertFalse(result.isPresent());
    }

    @Test
    void testDelete() {
        CartItem cartItem = new CartItem();
        doNothing().when(cartItemRepository).delete(cartItem);

        cartItemService.delete(cartItem);

        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void testFindByCartOrderByIdAsc() {
        Cart cart = new Cart();
        List<CartItem> expectedCartItems = List.of(new CartItem(), new CartItem());
        when(cartItemRepository.findByCartOrderByIdAsc(cart)).thenReturn(expectedCartItems);

        List<CartItem> result = cartItemService.findByCartOrderByIdAsc(cart);

        assertNotNull(result);
        assertEquals(expectedCartItems.size(), result.size());
    }
}
