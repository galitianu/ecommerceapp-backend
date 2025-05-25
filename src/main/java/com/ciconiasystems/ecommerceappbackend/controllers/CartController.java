package com.ciconiasystems.ecommerceappbackend.controllers;

import com.ciconiasystems.ecommerceappbackend.entities.Cart;
import com.ciconiasystems.ecommerceappbackend.entities.CartItem;
import com.ciconiasystems.ecommerceappbackend.entities.Product;
import com.ciconiasystems.ecommerceappbackend.entities.User;
import com.ciconiasystems.ecommerceappbackend.exceptions.ErrorCode;
import com.ciconiasystems.ecommerceappbackend.exceptions.ForbiddenException;
import com.ciconiasystems.ecommerceappbackend.exceptions.ValidationException;
import com.ciconiasystems.ecommerceappbackend.services.CartService;
import com.ciconiasystems.ecommerceappbackend.services.ProductService;
import com.ciconiasystems.ecommerceappbackend.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/cart/items")
@RequiredArgsConstructor
public class CartController extends BaseController {
    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartItem> addProductToCart(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId, @RequestParam UUID productID, @RequestParam int quantity) {
        if (cartService.checkQuantityNotGreaterThanZero(quantity)) {
            throw new ValidationException(ErrorCode.INVALID_QUANTITY);
        }
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Product product = findProductById(productID);
        if (product.isDisabled()) {
            throw new ForbiddenException(ErrorCode.DISABLED_PRODUCT);
        }
        Cart cart = cartService.findOrCreateCart(user);
        CartItem cartItem = cartService.addProduct(cart, product, quantity);
        return ResponseEntity.ok(cartItem);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<CartItem> getCart(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Cart cart = cartService.findCart(user);
        return cartService.getCartItems(cart);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<Void> removeAllCartItems(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        cartService.removeAllItemsFromCart(user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartItem> patchProductToCart(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId, @RequestParam UUID productID, @RequestParam int quantity) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Product product = findProductById(productID);
        if (product.isDisabled()) {
            throw new ForbiddenException(ErrorCode.DISABLED_PRODUCT);
        }
        Cart cart = cartService.findOrCreateCart(user);
        CartItem cartItem = cartService.addQuantityToProduct(cart, product, quantity);
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeCartItem(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId, @PathVariable UUID cartItemId) {
        User user = findUserById(userId);
        userService.checkIfUserMatchesJwt(user, jwt);
        Cart cart = cartService.findOrCreateCart(user);
        Optional<CartItem> optionalCartItem = cartService.findCartItemById(cartItemId);
        if (optionalCartItem.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_CART_ITEM);
        }
        if (!cartService.checkIfCartItemBelongsToCart(cart, optionalCartItem.get())) {
            throw new ValidationException(ErrorCode.CART_ITEM_DOES_NOT_BELONG_TO_CART);
        }
        cartService.removeCartItem(optionalCartItem.get());
        return ResponseEntity.noContent().build();
    }


    private User findUserById(UUID userId) {
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_USER);
        }
        return optionalUser.get();
    }

    private Product findProductById(UUID productId) {
        Optional<Product> optionalProduct = productService.findProductById(productId);
        if (optionalProduct.isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_PRODUCT);
        }
        return optionalProduct.get();
    }
}
