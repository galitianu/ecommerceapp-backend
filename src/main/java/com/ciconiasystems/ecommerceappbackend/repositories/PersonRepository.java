package com.ciconiasystems.ecommerceappbackend.repositories;

import com.ciconiasystems.ecommerceappbackend.entities.Person;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByUser(User user);
}
