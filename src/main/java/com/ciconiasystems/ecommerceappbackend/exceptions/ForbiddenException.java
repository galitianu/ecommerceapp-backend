package com.ciconiasystems.ecommerceappbackend.exceptions;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {
    private final ErrorCode errorCode;

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}