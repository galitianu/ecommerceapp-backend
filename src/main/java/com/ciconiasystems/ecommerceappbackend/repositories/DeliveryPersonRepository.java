package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.DeliveryPerson;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, UUID> {
    Optional<DeliveryPerson> findByUser(User user);
}
