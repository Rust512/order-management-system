package com.design.order_management_system.utils;

import com.design.order_management_system.dto.common.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static ResponseEntity<ApiErrorResponse> getApiErrorResponseEntity(Exception ex, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(ApiErrorResponse.builder()
                        .exceptionName(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .build());
    }
}
