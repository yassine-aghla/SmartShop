package com.smartshop.exceptions;

public class PromocodeNotFoundException extends RuntimeException {
    public PromocodeNotFoundException(String message) {
        super(message);
    }

    public PromocodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
