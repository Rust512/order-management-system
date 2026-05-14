package com.design.order_management_system.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class ApiErrorResponse {
    @JsonProperty(value = "dStatusCode")
    private int statusCode;

    @JsonProperty(value = "sError")
    private String error;

    @JsonProperty(value = "sExceptionName")
    private String exceptionName;

    @JsonProperty(value = "sMessage")
    private String message;

    @JsonProperty(value = "sPath")
    private String path;

    @JsonProperty(value = "dtTimeStamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
