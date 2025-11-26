package com.smartshop.exceptions;

class UserBusinessException extends RuntimeException {
    public UserBusinessException(String message) {
        super(message);
    }

    public UserBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
