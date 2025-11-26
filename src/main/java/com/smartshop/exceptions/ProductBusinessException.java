package com.smartshop.exceptions;

public class ProductBusinessException extends RuntimeException {
    public ProductBusinessException(String message) {
        super(message);
    }

    public ProductBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
