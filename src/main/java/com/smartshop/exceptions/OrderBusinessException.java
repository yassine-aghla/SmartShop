package com.smartshop.exceptions;


public class OrderBusinessException extends RuntimeException {
    public OrderBusinessException(String message) {
        super(message);
    }

    public OrderBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
