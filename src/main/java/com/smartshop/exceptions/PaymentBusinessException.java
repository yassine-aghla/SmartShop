package com.smartshop.exceptions;


public class PaymentBusinessException extends RuntimeException {
    public PaymentBusinessException(String message) {
        super(message);
    }

    public PaymentBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
