package com.design.order_management_system.factory;

import com.design.order_management_system.dto.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public class ErrorMessageFactory {
    private ErrorMessageFactory() {
    }

    public static ResponseEntity<ApiErrorResponse> getApiErrorResponseEntity(
            Exception ex,
            HttpStatus httpStatus,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(httpStatus)
                .body(ApiErrorResponse.builder()
                        .statusCode(httpStatus.value())
                        .error(httpStatus.getReasonPhrase())
                        .exceptionName(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build());
    }
}
