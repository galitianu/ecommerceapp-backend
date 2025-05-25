package com.ciconiasystems.ecommerceappbackend.controllers;

import com.ciconiasystems.ecommerceappbackend.entities.DeliveryPerson;
import com.ciconiasystems.ecommerceappbackend.entities.Order;
import com.ciconiasystems.ecommerceappbackend.entities.OrderItem;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Optional;


@RestController
@RequestMapping("/delivery-person")
@RequiredArgsConstructor
@Slf4j
public class DeliveryPersonController {
    private final DeliveryPersonService deliveryPersonService;
    private final UserService userService;
    private final KeycloakService keycloakService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @GetMapping("/self")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> addUser(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        User user = userService.findOrCreateUser(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(String lastName, String firstName, String email, String password) {
        if (!keycloakService.createNewMobileUser(email, email, firstName, lastName, password))
            return ResponseEntity.badRequest().build();
        User user = userService.findOrCreateUser(email);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}/person")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryPerson> updateDeliveryPerson(@AuthenticationPrincipal Jwt jwt,
                                                               @PathVariable UUID userId,
                                                               @RequestParam(required = false) String firstName,
                                                               @RequestParam(required = false) String lastName) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(user.getUsername(), jwt);
        if(firstName == null)
            firstName = deliveryPerson.getFirstName();
        if(lastName == null)
            lastName = deliveryPerson.getLastName();
        if(keycloakService.patchUserMobile(user.getUsername(),firstName, lastName)) {
            return ResponseEntity.ok(deliveryPersonService.updateDeliveryPerson(deliveryPerson, firstName, lastName));
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{userId}/confirm-order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public boolean confirmNewOrder(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable UUID userId,
                                @PathVariable UUID orderId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(user.getUsername(), jwt);
        Optional<Order> order = orderService.getOrderAsDeliveryPerson(orderId);
        return orderService.setDeliveryPerson(order.get(), deliveryPerson);
    }

    @GetMapping("/{userId}/ongoing-order")
    @PreAuthorize("isAuthenticated()")
    public List<Order> getAllOngoingOrders(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(user.getUsername(), jwt);
        return orderService.getOngoingOrdersForADeliveryPerson(deliveryPerson);
    }

    @PatchMapping("/{userId}/deliver-order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public boolean confirmOrderAsDelivered(@AuthenticationPrincipal Jwt jwt,
                                   @PathVariable UUID userId,
                                   @PathVariable UUID orderId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(user.getUsername(), jwt);
        Optional<Order> order = orderService.getOrderAsDeliveryPerson(orderId);
        return order.filter(value -> orderService.confirmOrder(value, deliveryPerson)).isPresent();
    }

    @GetMapping("/{userId}/delivered-order")
    @PreAuthorize("isAuthenticated()")
    public List<Order> getAllDeliveredOrders(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(user.getUsername(), jwt);
        return orderService.getDeliveredOrdersForADeliveryPerson(deliveryPerson);
    }

    @GetMapping("/{userId}/person")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryPerson> getSelfDeliveryPerson(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        String username = jwt.getClaim("preferred_username");
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(username, jwt);
        return ResponseEntity.ok(deliveryPerson);
    }

    @GetMapping("/{userId}/{orderId}/orderItems")
    @PreAuthorize("isAuthenticated()")
    public List<OrderItem> getOrderItemsByOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId, @PathVariable UUID orderId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        String username = jwt.getClaim("preferred_username");
        DeliveryPerson deliveryPerson = deliveryPersonService.findOrCreateDeliveryPerson(username, jwt);
        Order order = orderService.getOrderAsDeliveryPerson(deliveryPerson, orderId)
                .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_ORDER));
        return orderItemService.findByOrderOrderByIdAsc(order);
    }


    private User findUserById(UUID userId) {
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_USER);
        }
        return optionalUser.get();
    }
}
