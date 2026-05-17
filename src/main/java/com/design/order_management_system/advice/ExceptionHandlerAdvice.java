package com.design.order_management_system.advice;

import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.InvalidCredentialsException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.exception.RevokedTokenException;
import com.design.order_management_system.factory.ErrorMessageFactory;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(value = DuplicateResourceException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = InsufficientResourcesException.class)
    ResponseEntity<ApiErrorResponse> handleInsufficientResourcesException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.UNPROCESSABLE_CONTENT, request);
    }

    @ExceptionHandler(value = {
            InvalidCredentialsException.class,
            RevokedTokenException.class,
            JwtException.class
    })
    ResponseEntity<ApiErrorResponse> handleInvalidCredentialsException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return ErrorMessageFactory.getApiErrorResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
