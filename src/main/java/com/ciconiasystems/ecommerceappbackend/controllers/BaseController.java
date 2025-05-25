package com.ciconiasystems.ecommerceappbackend.controllers;

import org.springframework.http.ResponseEntity;

import java.util.Optional;

public abstract class BaseController {

    protected <T> ResponseEntity<T> responseFromOptional(Optional<T> optional) {
        return optional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
