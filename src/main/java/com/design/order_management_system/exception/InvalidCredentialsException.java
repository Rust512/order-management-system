package com.design.order_management_system.exception;

import com.design.order_management_system.constants.ErrorMessageConstants;

public class InvalidCredentialsException extends RuntimeException {

    private InvalidCredentialsException(String message) {
        super(message);
    }

    public static InvalidCredentialsException forUsername(String username) {
        return new InvalidCredentialsException(String.format(ErrorMessageConstants.PASSWORD_IS_INVALID, username));
    }
}
