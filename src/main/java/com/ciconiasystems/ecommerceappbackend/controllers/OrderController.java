package com.ciconiasystems.ecommerceappbackend.controllers;

import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.services.KeycloakService;
import com.ciconiasystems.ecommerceappbackend.services.OrderService;
import com.ciconiasystems.ecommerceappbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final KeycloakService keycloakService;

    @GetMapping("/undelivered")
    public List<Order> getAllUndeliveredOrders() {
        return orderService.findAllUndeliveredOrders();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<Order> getAllOrders(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        String email = user.getUsername();
        boolean isAdmin = keycloakService.isWebUserAdmin(email);
        if (isAdmin) {
            return orderService.findAll();
        } else {
            return List.of();
        }
    }

    @PostMapping("/confirm-payment")
    public Order confirmPayment(@RequestBody Order order) {
        return orderService.confirmPayment(order);
    }

    private User findUserById(UUID userId) {
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_USER);
        }
        return optionalUser.get();
    }
}
