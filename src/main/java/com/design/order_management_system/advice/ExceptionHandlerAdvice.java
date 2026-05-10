package com.design.order_management_system.advice;

import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.utils.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(value = DuplicateResourceException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ExceptionUtils.getApiErrorResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ExceptionUtils.getApiErrorResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InsufficientResourcesException.class)
    ResponseEntity<ApiErrorResponse> handleInsufficientResourcesException(InsufficientResourcesException ex) {
        return ExceptionUtils.getApiErrorResponseEntity(ex, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        return ExceptionUtils.getApiErrorResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
