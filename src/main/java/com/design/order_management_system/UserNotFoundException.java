package com.design.order_management_system;

public class UserNotFoundException extends RuntimeException {

    public static final String USERNAME_NOT_FOUND = "user with username %s not found";
    public static final String ID_NOT_FOUND = "user with id %s not found";

    public UserNotFoundException(String username) {
        super(String.format(USERNAME_NOT_FOUND, username));
    }

    public UserNotFoundException(Long id) {
        super(String.format(ID_NOT_FOUND, id));
    }
}
