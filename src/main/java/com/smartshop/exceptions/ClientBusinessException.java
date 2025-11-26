package com.smartshop.exceptions;

public class ClientBusinessException extends RuntimeException {
    public ClientBusinessException(String message) {
        super(message);
    }

    public ClientBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
