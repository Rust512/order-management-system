package com.design.order_management_system.exception;

import com.design.order_management_system.constants.ErrorMessageConstants;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(String.format(ErrorMessageConstants.ALREADY_EXISTS, resourceName, fieldName, fieldValue));
    }
}
