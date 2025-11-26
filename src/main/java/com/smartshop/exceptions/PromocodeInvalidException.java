package com.smartshop.exceptions;

public class PromocodeInvalidException extends RuntimeException {
    public PromocodeInvalidException(String message) {
        super(message);
    }

    public PromocodeInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
