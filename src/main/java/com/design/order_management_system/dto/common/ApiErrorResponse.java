package com.design.order_management_system.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
@AllArgsConstructor
public class ApiErrorResponse {

    @NotBlank
    @JsonProperty(value = "sMessage")
    private String message;

    @NotBlank
    @JsonProperty(value = "sExceptionName")
    private String exceptionName;
}
