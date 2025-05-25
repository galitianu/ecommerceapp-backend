package com.ciconiasystems.ecommerceappbackend;

import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ForbiddenException;
import com.ciconiasystems.ecommerceappbackend.repositories.UserRepository;
import com.ciconiasystems.ecommerceappbackend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals(user.getId(), savedUser.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindOrCreateUser_UserExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        User foundUser = userService.findOrCreateUser("testuser");

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindOrCreateUser_UserDoesNotExist() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.findOrCreateUser("testuser");

        assertNotNull(createdUser);
        assertEquals(user.getId(), createdUser.getId());
        assertEquals("testuser", createdUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testFindUserById() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findUserById(user.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(user.getId(), foundUser.get().getId());
        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    void testCheckIfUserMatchesJwt_ValidUser() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");

        assertDoesNotThrow(() -> userService.checkIfUserMatchesJwt(user, jwt));
        verify(jwt, times(1)).getClaimAsString("preferred_username");
    }

    @Test
    void testCheckIfUserMatchesJwt_InvalidUser() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("wronguser");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userService.checkIfUserMatchesJwt(user, jwt));
        assertEquals(ErrorCode.INVALID_USER, exception.getErrorCode());
        verify(jwt, times(1)).getClaimAsString("preferred_username");
    }
}
