package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.DeliveryPerson;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.repositories.DeliveryPersonRepository;
import com.ciconiasystems.ecommerceappbackend.services.DeliveryPersonService;
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
class DeliveryPersonServiceTest {

    @Mock
    private DeliveryPersonRepository deliveryPersonRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DeliveryPersonService deliveryPersonService;

    private User user;
    private DeliveryPerson deliveryPerson;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");

        deliveryPerson = new DeliveryPerson();
        deliveryPerson.setUser(user);
        deliveryPerson.setFirstName("John");
        deliveryPerson.setLastName("Doe");

        jwt = mock(Jwt.class);

        // Use lenient stubbing to avoid UnnecessaryStubbingException
        lenient().when(jwt.getClaim("given_name")).thenReturn("John");
        lenient().when(jwt.getClaim("family_name")).thenReturn("Doe");
    }

    @Test
    void testFindOrCreateDeliveryPerson_UserExists_DeliveryPersonExists() {
        when(userService.findOrCreateUser(anyString())).thenReturn(user);
        when(deliveryPersonRepository.findByUser(any(User.class))).thenReturn(Optional.of(deliveryPerson));

        DeliveryPerson result = deliveryPersonService.findOrCreateDeliveryPerson("testuser", jwt);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userService, times(1)).findOrCreateUser("testuser");
        verify(deliveryPersonRepository, times(1)).findByUser(user);
        verify(deliveryPersonRepository, never()).save(any(DeliveryPerson.class));
    }

    @Test
    void testFindOrCreateDeliveryPerson_UserExists_DeliveryPersonDoesNotExist() {
        when(userService.findOrCreateUser(anyString())).thenReturn(user);
        when(deliveryPersonRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(deliveryPersonRepository.save(any(DeliveryPerson.class))).thenReturn(deliveryPerson);

        DeliveryPerson result = deliveryPersonService.findOrCreateDeliveryPerson("testuser", jwt);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userService, times(1)).findOrCreateUser("testuser");
        verify(deliveryPersonRepository, times(1)).findByUser(user);
        verify(deliveryPersonRepository, times(1)).save(any(DeliveryPerson.class));
    }

    @Test
    void testUpdateDeliveryPerson() {
        when(deliveryPersonRepository.save(any(DeliveryPerson.class))).thenReturn(deliveryPerson);

        DeliveryPerson result = deliveryPersonService.updateDeliveryPerson(deliveryPerson, "Jane", "Smith");

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        verify(deliveryPersonRepository, times(1)).save(deliveryPerson);
    }
}
