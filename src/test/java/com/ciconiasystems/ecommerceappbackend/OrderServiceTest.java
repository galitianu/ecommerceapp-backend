package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.*;
import com.ciconiasystems.ecommerceappbackend.repositories.OrderRepository;
import com.ciconiasystems.ecommerceappbackend.services.OrderItemService;
import com.ciconiasystems.ecommerceappbackend.services.OrderService;
import com.ciconiasystems.ecommerceappbackend.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    private Person person;
    private BillingInformation billingInformation;
    private List<CartItem> cartItemList;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(UUID.randomUUID());

        billingInformation = new BillingInformation();
        billingInformation.setPaymentOption(PaymentOption.ON_DELIVERY);

        cartItemList = new ArrayList<>();

        Product product = new Product();
        product.setPrice(10.0);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItemList.add(cartItem);

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setPerson(person);
        order.setDatePlaced(ZonedDateTime.now());
        order.setBillingInformation(billingInformation);

        orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(10.0);
        orderItem.setOrder(order);
    }

    @Test
    void testCreateNewOrderWithOrderItems_OnDelivery() throws Exception {
        when(orderItemService.addOrderItemToOrder(anyList(), any(Order.class))).thenReturn(List.of(orderItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createNewOrderWithOrderItems(person, billingInformation, cartItemList);

        assertNotNull(result);
        assertEquals(person, result.getPerson());
        assertEquals(billingInformation, result.getBillingInformation());
        assertEquals(20.0, result.getTotal());
        assertFalse(result.isPending());
        verify(orderItemService, times(1)).addOrderItemToOrder(anyList(), any(Order.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentService, never()).createPaymentIntent(anyLong(), anyString(), any(Order.class));
    }

    @Test
    void testCreateNewOrderWithOrderItems_OnlinePayment() throws Exception {
        billingInformation.setPaymentOption(PaymentOption.ONLINE_PAYMENT);

        when(orderItemService.addOrderItemToOrder(anyList(), any(Order.class))).thenReturn(List.of(orderItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createNewOrderWithOrderItems(person, billingInformation, cartItemList);

        assertNotNull(result);
        assertEquals(person, result.getPerson());
        assertEquals(billingInformation, result.getBillingInformation());
        assertEquals(20.0, result.getTotal());
        assertTrue(result.isPending());
        verify(orderItemService, times(1)).addOrderItemToOrder(anyList(), any(Order.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentService, times(1)).createPaymentIntent(2000L, "usd", result);
    }

    @Test
    void testCalculateOrderTotal() {
        List<OrderItem> orderItems = List.of(orderItem);
        double total = orderService.calculateOrderTotal(orderItems);

        assertEquals(20.0, total);
    }

    @Test
    void testGetOrders() {
        when(orderRepository.findByPerson(any(Person.class))).thenReturn(List.of(order));

        List<Order> result = orderService.getOrders(person);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository, times(1)).findByPerson(any(Person.class));
    }

    @Test
    void testGetOrder() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrder(person, order.getId());

        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testGetOrderAsDeliveryPerson() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderAsDeliveryPerson(order.getId());

        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testGetOrderAsDeliveryPerson_WithDeliveryPerson() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderAsDeliveryPerson(deliveryPerson, order.getId());

        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void testFindAllUndeliveredOrders() {
        when(orderRepository.findByDeliveredFalseAndDeliveryPersonIsNull()).thenReturn(List.of(order));

        List<Order> result = orderService.findAllUndeliveredOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository, times(1)).findByDeliveredFalseAndDeliveryPersonIsNull();
    }

    @Test
    void testFindAll() {
        when(orderRepository.findAllByOrderByDatePlacedDesc()).thenReturn(List.of(order));

        List<Order> result = orderService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository, times(1)).findAllByOrderByDatePlacedDesc();
    }

    @Test
    void testSetDeliveryPerson_Success() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        boolean result = orderService.setDeliveryPerson(order, deliveryPerson);

        assertTrue(result);
        assertEquals(deliveryPerson, order.getDeliveryPerson());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testSetDeliveryPerson_Failure() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        order.setDelivered(true);

        boolean result = orderService.setDeliveryPerson(order, deliveryPerson);

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOngoingOrdersForADeliveryPerson() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        when(orderRepository.findByDeliveryPersonAndDeliveredFalse(any(DeliveryPerson.class))).thenReturn(List.of(order));

        List<Order> result = orderService.getOngoingOrdersForADeliveryPerson(deliveryPerson);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository, times(1)).findByDeliveryPersonAndDeliveredFalse(any(DeliveryPerson.class));
    }

    @Test
    void testConfirmOrder_Success() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        order.setDeliveryPerson(deliveryPerson);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        boolean result = orderService.confirmOrder(order, deliveryPerson);

        assertTrue(result);
        assertTrue(order.isDelivered());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testConfirmOrder_Failure() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();

        boolean result = orderService.confirmOrder(order, deliveryPerson);

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testConfirmOrder_Failure_NotDeliveryPerson() {
        DeliveryPerson correctDeliveryPerson = new DeliveryPerson();
        DeliveryPerson incorrectDeliveryPerson = new DeliveryPerson();
        order.setDeliveryPerson(correctDeliveryPerson);

        boolean result = orderService.confirmOrder(order, incorrectDeliveryPerson);

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testConfirmOrder_Failure_AlreadyDelivered() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        order.setDeliveryPerson(deliveryPerson);
        order.setDelivered(true);

        boolean result = orderService.confirmOrder(order, deliveryPerson);

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetDeliveredOrdersForADeliveryPerson() {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        when(orderRepository.findByDeliveryPersonAndDeliveredTrue(any(DeliveryPerson.class))).thenReturn(List.of(order));

        List<Order> result = orderService.getDeliveredOrdersForADeliveryPerson(deliveryPerson);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(order, result.get(0));
        verify(orderRepository, times(1)).findByDeliveryPersonAndDeliveredTrue(any(DeliveryPerson.class));
    }

    @Test
    void testGetOrderAsAdmin() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderAsAdmin(order.getId());

        assertTrue(result.isPresent());
        assertEquals(order, result.get());
        verify(orderRepository, times(1)).findById(any(UUID.class));
    }
}
