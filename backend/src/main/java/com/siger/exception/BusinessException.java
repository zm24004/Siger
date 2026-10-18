package com.siger.exception;

/**
 * Raised for business-rule violations (duplicate reservation, capacity full,
 * invalid state transition, etc.). Mapped to HTTP 409 by GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
