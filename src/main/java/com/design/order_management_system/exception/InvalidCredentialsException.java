package com.design.order_management_system.exception;

public class InvalidCredentialsException extends RuntimeException {

    private static final String PASSWORD_IS_INVALID = "Given password for username %s is invalid";

    private InvalidCredentialsException(String message) {
        super(message);
    }

    public static InvalidCredentialsException forUsername(String username) {
        return new InvalidCredentialsException(String.format(PASSWORD_IS_INVALID, username));
    }
}
