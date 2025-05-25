package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.DeliveryPerson;
import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.ciconiasystems.ecommerceappbackend.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByPerson(Person person);

    @Query("SELECT o FROM Order o WHERE o.delivered = false AND o.deliveryPerson IS NULL AND o.pending = false")
    List<Order> findUndeliveredOrdersWithoutDeliveryPersonAndNotPending();

    List<Order> findByDeliveryPersonAndDeliveredFalse(DeliveryPerson deliveryPerson);

    List<Order> findByDeliveryPersonAndDeliveredTrue(DeliveryPerson deliveryPerson);

    @Query("SELECT o FROM Order o ORDER BY o.datePlaced DESC")
    List<Order> findAllByOrderByDatePlacedDesc();
}
