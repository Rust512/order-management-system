package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(@JsonProperty(value = "sToken") String token) {
}
