package com.design.order_management_system.exception;

import com.design.order_management_system.constants.ErrorMessageConstants;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, resourceName, fieldName, fieldValue));
    }
}
