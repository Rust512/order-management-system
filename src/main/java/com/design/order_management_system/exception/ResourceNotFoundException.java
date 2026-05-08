package com.design.order_management_system.exception;

public class ResourceNotFoundException extends RuntimeException {

    public static final String RESOURCE_NOT_FOUND = "Resource %s with %s matching %s not found";

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format(RESOURCE_NOT_FOUND, resourceName, fieldName, fieldValue));
    }
}
