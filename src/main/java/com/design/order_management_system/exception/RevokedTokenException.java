package com.design.order_management_system.exception;

public class RevokedTokenException extends RuntimeException {
    public RevokedTokenException(String message) {
        super(message);
    }
}
