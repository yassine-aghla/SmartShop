package com.smartshop.exceptions;

public class PromocodeBusinessException extends RuntimeException {
    public PromocodeBusinessException(String message) {
        super(message);
    }

    public PromocodeBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}