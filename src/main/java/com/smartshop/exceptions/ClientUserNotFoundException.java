package com.smartshop.exceptions;

public class ClientUserNotFoundException extends RuntimeException {
    public ClientUserNotFoundException(String message) {
        super(message);
    }

    public ClientUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
