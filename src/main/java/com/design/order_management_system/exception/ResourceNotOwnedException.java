package com.design.order_management_system.exception;

import com.design.order_management_system.constants.ErrorMessageConstants;

public class ResourceNotOwnedException extends RuntimeException {
    public ResourceNotOwnedException(
            String resourceName, String fieldName, String fieldValue, Long userId) {
        super(
                String.format(
                        ErrorMessageConstants.RESOURCE_NOT_OWNED,
                        resourceName,
                        fieldName,
                        fieldValue,
                        userId));
    }
}
