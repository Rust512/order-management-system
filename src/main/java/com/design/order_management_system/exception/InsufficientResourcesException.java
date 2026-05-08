package com.design.order_management_system.exception;

public class InsufficientResourcesException extends RuntimeException {

    public static final String INSUFFICIENT_RESOURCES = "Insufficient resources for %s: requested value of %s was %d, but only %d was found";

    public InsufficientResourcesException(String resourceName, String fieldName, long minValue, long fieldValue) {
        super(String.format(INSUFFICIENT_RESOURCES, resourceName, fieldName, minValue, fieldValue));
    }
}
