package com.design.order_management_system.exception;

public class DuplicateResourceException extends RuntimeException {

    public static final String ALREADY_EXISTS = "Resource %s with %s matching %s already exists";

    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(String.format(ALREADY_EXISTS, resourceName, fieldName, fieldValue));
    }
}
