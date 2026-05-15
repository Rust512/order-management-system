package com.design.order_management_system.exception;

import com.design.order_management_system.constants.ErrorMessageConstants;

public class InsufficientResourcesException extends RuntimeException {

    public InsufficientResourcesException(String resourceName, String fieldName, long minValue, long fieldValue) {
        super(String.format(ErrorMessageConstants.INSUFFICIENT_RESOURCES, resourceName, fieldName, minValue, fieldValue));
    }
}
