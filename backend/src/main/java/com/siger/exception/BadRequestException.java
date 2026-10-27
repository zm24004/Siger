package com.siger.exception;

/**
 * Raised when request parameters are syntactically valid but inconsistent.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
