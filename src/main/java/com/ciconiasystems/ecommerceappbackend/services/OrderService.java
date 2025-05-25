package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.*;
import com.ciconiasystems.ecommerceappbackend.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final PaymentService paymentService;

    @SneakyThrows
    public Order createNewOrderWithOrderItems(Person person, BillingInformation billingInformation, List<CartItem> cartItemList) {
        Order newOrder = new Order();
        newOrder.setPerson(person);
        newOrder.setDatePlaced(ZonedDateTime.now());
        newOrder.setBillingInformation(billingInformation);
        List<OrderItem> orderItems = orderItemService.addOrderItemToOrder(cartItemList, newOrder);
        double amount = calculateOrderTotal(orderItems);
        orderRepository.save(newOrder);
        if (newOrder.getBillingInformation().getPaymentOption() == PaymentOption.ON_DELIVERY) {
            newOrder.setPending(false);
        } else {
            Long amountLong = (long) (amount * 100);
            paymentService.createPaymentIntent(amountLong, "usd", newOrder);
        }
        newOrder.setTotal(amount);
        return newOrder;
    }

    public double calculateOrderTotal(List<OrderItem> orderItems) {
        double total = 0;
        for (OrderItem orderItem : orderItems) {
            total += orderItem.getPrice() * orderItem.getQuantity();
        }
        return total;
    }

    public List<Order> getOrders(Person person) {
        return orderRepository.findByPerson(person);
    }

    public Optional<Order> getOrder(Person person, UUID orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent() && order.get().getPerson().equals(person)) {
            return order;
        }
        return Optional.empty();
    }

    public Optional<Order> getOrderAsDeliveryPerson(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    public Optional<Order> getOrderAsDeliveryPerson(DeliveryPerson deliveryPerson, UUID orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent() && deliveryPerson!= null && !order.get().isPending()) {
            return order;
        }
        return Optional.empty();
    }

    public List<Order> findAllUndeliveredOrders() {
        return orderRepository.findUndeliveredOrdersWithoutDeliveryPersonAndNotPending();
    }

    public List<Order> findAll() {
        return orderRepository.findAllByOrderByDatePlacedDesc();
    }

    public boolean setDeliveryPerson(Order order, DeliveryPerson deliveryPerson) {
        if (order.isDelivered()) {
            return false;
        }
        if (order.getDeliveryPerson() != null) {
            return false;
        }
        order.setDeliveryPerson(deliveryPerson);
        orderRepository.save(order);
        return true;
    }

    public List<Order> getOngoingOrdersForADeliveryPerson(DeliveryPerson deliveryPerson) {
        return orderRepository.findByDeliveryPersonAndDeliveredFalse(deliveryPerson);
    }

    public boolean confirmOrder(Order order, DeliveryPerson deliveryPerson) {
        if (order.getDeliveryPerson() != deliveryPerson) {
            return false;
        }
        if (order.isDelivered()) {
            return false;
        }
        order.setDelivered(true);
        orderRepository.save(order);
        return true;
    }

    public List<Order> getDeliveredOrdersForADeliveryPerson(DeliveryPerson deliveryPerson) {
        return orderRepository.findByDeliveryPersonAndDeliveredTrue(deliveryPerson);
    }

    public Optional<Order>  getOrderAsAdmin(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @SneakyThrows
    public Order confirmPayment(Order order) {
        boolean paid = paymentService.isPaymentSuccessful(order.getStripeClientSecret());
        if (paid)
        {
            order.setPending(false);
            return orderRepository.save(order);
        }
        return order;
    }
}
