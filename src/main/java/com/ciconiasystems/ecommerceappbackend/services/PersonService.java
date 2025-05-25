package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.Person;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final UserService userService;

    public Person findOrCreatePerson(String username, Jwt jwt) {
        User user = userService.findOrCreateUser(username);
        return personRepository.findByUser(user).orElseGet(() -> {
            Person person = new Person();
            person.setUser(user);
            person.setFirstName(jwt.getClaim("given_name").toString());
            person.setLastName(jwt.getClaim("family_name").toString());
            return personRepository.save(person);
        });
    }
}
