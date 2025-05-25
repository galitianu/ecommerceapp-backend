package com.ciconiasystems.ecommerceappbackend.services;

import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ForbiddenException;
import com.ciconiasystems.ecommerceappbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findOrCreateUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            return saveUser(user);
        });
    }

    public Optional<User> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public void checkIfUserMatchesJwt(User user, Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (!user.getUsername().equals(username)) {
            throw new ForbiddenException(ErrorCode.INVALID_USER);
        }
    }
}