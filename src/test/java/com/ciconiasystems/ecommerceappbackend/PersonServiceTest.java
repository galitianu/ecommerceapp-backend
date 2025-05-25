package com.ciconiasystems.ecommerceappbackend;


import com.ciconiasystems.ecommerceappbackend.entities.Person;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.repositories.PersonRepository;
import com.ciconiasystems.ecommerceappbackend.services.PersonService;
import com.ciconiasystems.ecommerceappbackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PersonService personService;

    private User user;
    private Person person;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");

        person = new Person();
        person.setUser(user);
        person.setFirstName("John");
        person.setLastName("Doe");

        jwt = mock(Jwt.class);

        // Use lenient stubbing to avoid UnnecessaryStubbingException
        lenient().when(jwt.getClaim("given_name")).thenReturn("John");
        lenient().when(jwt.getClaim("family_name")).thenReturn("Doe");
    }

    @Test
    void testFindOrCreatePerson_PersonExists() {
        when(userService.findOrCreateUser(anyString())).thenReturn(user);
        when(personRepository.findByUser(any(User.class))).thenReturn(Optional.of(person));

        Person result = personService.findOrCreatePerson("testuser", jwt);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userService, times(1)).findOrCreateUser("testuser");
        verify(personRepository, times(1)).findByUser(user);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void testFindOrCreatePerson_PersonDoesNotExist() {
        when(userService.findOrCreateUser(anyString())).thenReturn(user);
        when(personRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenReturn(person);

        Person result = personService.findOrCreatePerson("testuser", jwt);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userService, times(1)).findOrCreateUser("testuser");
        verify(personRepository, times(1)).findByUser(user);
        verify(personRepository, times(1)).save(any(Person.class));
    }
}
