package com.smartshop.exceptions;

public class PromocodeAlreadyExistsException extends RuntimeException {
    public PromocodeAlreadyExistsException(String message) {
        super(message);
    }

    public PromocodeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
