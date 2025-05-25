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
public class ConflictExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorMessage> handleDuplicationException(ConflictException exception) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exception.getErrorCode().toString()
        );
        log.info("Exception thrown as 409 CONFLICT: " + exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }
}
