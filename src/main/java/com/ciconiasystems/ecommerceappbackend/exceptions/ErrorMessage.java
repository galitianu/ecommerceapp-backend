package com.ciconiasystems.ecommerceappbackend.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class ErrorMessage {
    private HttpStatus status;
    private String message;
    private String errorCode;
}