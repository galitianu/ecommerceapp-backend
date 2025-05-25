package com.ciconiasystems.ecommerceappbackend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_CATEGORY("Invalid category provided."),
    INVALID_IMAGE("Invalid image path provided."),
    INVALID_PRODUCT("Invalid product provided."),
    DUPLICATE_SLUG("Product slug already exists."),
    INVALID_USER("Invalid user provided."),
    INVALID_CART_ITEM("Invalid cart item provided."),
    CART_ITEM_DOES_NOT_BELONG_TO_CART("Cart item does not belong to cart."),
    INVALID_QUANTITY("Cart item quantity should be greater than 0."),
    DUPLICATE_PRODUCT_IN_CART("Product already exists in cart."),
    EXISTING_USER("User already exists."),
    INVALID_ORDER("Invalid order provided."),
    EMPTY_CART("Cart is empty."),
    DISABLED_PRODUCT("Product is disabled");

    private final String message;
}
