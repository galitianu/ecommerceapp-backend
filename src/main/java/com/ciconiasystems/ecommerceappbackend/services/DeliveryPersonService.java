package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.DeliveryPerson;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.repositories.DeliveryPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryPersonService {
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final UserService userService;

    public DeliveryPerson findOrCreateDeliveryPerson(String username, Jwt jwt) {
        User user = userService.findOrCreateUser(username);
        return deliveryPersonRepository.findByUser(user).orElseGet(() -> {
            DeliveryPerson person = new DeliveryPerson();
            person.setUser(user);
            person.setFirstName(jwt.getClaim("given_name").toString());
            person.setLastName(jwt.getClaim("family_name").toString());
            return deliveryPersonRepository.save(person);
        });
    }

    public DeliveryPerson updateDeliveryPerson(DeliveryPerson deliveryPerson, String firstName, String lastName) {
        deliveryPerson.setFirstName(firstName);
        deliveryPerson.setLastName(lastName);
        deliveryPersonRepository.save(deliveryPerson);
        return deliveryPerson;
    }
}
