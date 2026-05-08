package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageDelegator {
    @JsonProperty(value = "sMessage")
    private String message;

    @JsonProperty(value = "sExceptionName")
    private String exceptionName;
}
