package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginResponse {
    @JsonProperty(value = "sToken")
    private final String token;
}
