package com.ciconiasystems.ecommerceappbackend.controllers;

import com.ciconiasystems.ecommerceappbackend.dto.BillingInformationDTO;
import com.ciconiasystems.ecommerceappbackend.entities.*;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.services.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final CartItemService cartItemService;
    private final CartService cartService;
    private final BillingInformationService billingInformationService;
    private final PersonService personService;
    private final OrderService orderService;
    private final KeycloakService keycloakService;
    private final OrderItemService orderItemService;
    private final EmailSenderService emailSenderService;
    private final AddressValidationService addressValidationService;

    @GetMapping("/self")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> addUser(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        User user = userService.findOrCreateUser(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping()
    public ResponseEntity<User> createUser(String lastName, String firstName, String email, String password) {
        if (!keycloakService.createNewWebUser(email, email, firstName, lastName, password))
            return ResponseEntity.badRequest().build();
        User user = userService.findOrCreateUser(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/orders")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @RequestBody BillingInformationDTO billingInfoRequest
    ) {
        final String targetCity = "Cluj";
        PlaceInformation validPlaceInformation = addressValidationService.getValidPlaceInformation(billingInfoRequest.getAddress(), billingInfoRequest.getCity(), targetCity);
        if (validPlaceInformation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid address or not in " + targetCity + ".");
        }
        BillingInformation billingInformation = new BillingInformation();
        billingInformation.setPhoneNumber(billingInfoRequest.getPhoneNumber());
        billingInformation.setAddress(billingInfoRequest.getAddress());
        billingInformation.setZipCode(billingInfoRequest.getZipCode());
        billingInformation.setCity(billingInfoRequest.getCity());
        billingInformation.setCountry(billingInfoRequest.getCountry());
        billingInformation.setPaymentOption(PaymentOption.valueOf(billingInfoRequest.getPaymentOption()));
        billingInformation.setPlaceInformation(validPlaceInformation);

        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Person person = personService.findOrCreatePerson(user.getUsername(), jwt);
        Cart cart = cartService.findCart(user);
        List<CartItem> cartItems = cartItemService.findByCartOrderByIdAsc(cart);
        if(cartItems.isEmpty()) {
            throw new ValidationException(ErrorCode.EMPTY_CART);
        }
        List<CartItem> disabledCartItems = cartItems.stream()
                .filter(cartItem -> cartItem.getProduct().isDisabled())
                .toList();
        if (!disabledCartItems.isEmpty()) {
            cartService.deleteDisabledCartItems(cart);
            return ResponseEntity.status(HttpStatus.GONE).body("Cart contains disabled products. They have been removed from your cart.");
        }
        billingInformationService.save(billingInformation);
        Order order = orderService.createNewOrderWithOrderItems(person, billingInformation, cartItems);
        cartItemService.deleteByCart(cart);
        emailSenderService.sendOrderConfirmationEmail(user.getUsername(), "Order Confirmation", orderItemService.findByOrderOrderByIdAsc(order));
        return ResponseEntity.ok(order);
    }



    @GetMapping("/{userId}/person")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Person> getSelfPerson(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        String username = jwt.getClaim("preferred_username");
        Person person = personService.findOrCreatePerson(username, jwt);
        return ResponseEntity.ok(person);
    }


    @GetMapping("/{userId}/orders")
    @PreAuthorize("isAuthenticated()")
    public List<Order> getOrders(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Person person = personService.findOrCreatePerson(user.getUsername(), jwt);
        return orderService.getOrders(person);
    }

    @GetMapping("/{userId}/{orderId}/orderItems")
    @PreAuthorize("isAuthenticated()")
    public List<OrderItem> getOrderItemsByOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId, @PathVariable UUID orderId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Person person = personService.findOrCreatePerson(user.getUsername(), jwt);
        boolean isAdmin = keycloakService.isWebUserAdmin(user.getUsername());
        Order order;
        if (isAdmin) {
            order = orderService.getOrderAsAdmin(orderId).orElseThrow(() -> new ValidationException(ErrorCode.INVALID_ORDER));
        } else {
            order = orderService.getOrder(person, orderId)
                    .orElseThrow(() -> new ValidationException(ErrorCode.INVALID_ORDER));
        }
        return orderItemService.findByOrderOrderByIdAsc(order);
    }

    @GetMapping("/{userId}/isAdmin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isUserAdmin(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        String email = user.getUsername();
        boolean isAdmin = keycloakService.isWebUserAdmin(email);
        return ResponseEntity.ok(isAdmin);
    }

    private User findUserById(UUID userId) {
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_USER);
        }
        return optionalUser.get();
    }
}
