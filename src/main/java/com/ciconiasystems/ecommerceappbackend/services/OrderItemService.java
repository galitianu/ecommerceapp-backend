package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.ciconiasystems.ecommerceappbackend.entities.OrderItem;
import com.ciconiasystems.ecommerceappbackend.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public List<OrderItem> addOrderItemToOrder(List<CartItem> cartItemList, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    public List<OrderItem> findByOrderOrderByIdAsc(Order order) {
        return orderItemRepository.findByOrderOrderByIdAsc(order);
    }
}
