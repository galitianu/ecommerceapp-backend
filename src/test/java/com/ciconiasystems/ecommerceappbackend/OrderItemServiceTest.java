package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.ciconiasystems.ecommerceappbackend.entities.OrderItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.repositories.OrderItemRepository;
import com.ciconiasystems.ecommerceappbackend.services.OrderItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private List<CartItem> cartItemList;
    private Order order;
    private OrderItem orderItem;
    private Product product;

    @BeforeEach
    void setUp() {
        cartItemList = new ArrayList<>();
        order = new Order();
        order.setId(UUID.randomUUID());

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(10.0);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItemList.add(cartItem);

        orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(10.0);
        orderItem.setOrder(order);
    }

    @Test
    void testAddOrderItemToOrder() {
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        List<OrderItem> result = orderItemService.addOrderItemToOrder(cartItemList, order);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(product, result.get(0).getProduct());
        assertEquals(2, result.get(0).getQuantity());
        assertEquals(10.0, result.get(0).getPrice());
        assertEquals(order, result.get(0).getOrder());
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    void testFindByOrderOrderByIdAsc() {
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);

        when(orderItemRepository.findByOrderOrderByIdAsc(any(Order.class))).thenReturn(orderItemList);

        List<OrderItem> result = orderItemService.findByOrderOrderByIdAsc(order);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderItem, result.get(0));
        verify(orderItemRepository, times(1)).findByOrderOrderByIdAsc(order);
    }
}
