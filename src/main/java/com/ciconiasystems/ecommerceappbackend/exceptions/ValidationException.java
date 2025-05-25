package com.ciconiasystems.ecommerceappbackend.exceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final ErrorCode errorCode;

    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}