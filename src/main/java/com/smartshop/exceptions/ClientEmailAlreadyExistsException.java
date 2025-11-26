package com.smartshop.exceptions;

public class ClientEmailAlreadyExistsException extends RuntimeException {
    public ClientEmailAlreadyExistsException(String message) {
        super(message);
    }

    public ClientEmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
