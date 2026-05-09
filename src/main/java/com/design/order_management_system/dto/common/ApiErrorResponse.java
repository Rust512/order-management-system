package com.design.order_management_system.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiErrorResponse {
    @JsonProperty(value = "sMessage")
    private String message;

    @JsonProperty(value = "sExceptionName")
    private String exceptionName;
}
