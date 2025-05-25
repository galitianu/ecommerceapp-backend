package com.ciconiasystems.ecommerceappbackend.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ForbiddenExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleForbiddenException(ForbiddenException exception) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                exception.getErrorCode().toString()
        );
        log.info("Exception thrown as 403 FORBIDDEN: " + exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }
}
