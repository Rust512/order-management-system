package com.design.order_management_system.advice;

import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.InvalidCredentialsException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.factory.ErrorMessageFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(value = DuplicateResourceException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InsufficientResourcesException.class)
    ResponseEntity<ApiErrorResponse> handleInsufficientResourcesException(InsufficientResourcesException ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(value = InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
