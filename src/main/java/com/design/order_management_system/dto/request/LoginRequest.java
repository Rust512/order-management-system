package com.design.order_management_system.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @JsonProperty(value = "sUsername")
    private String username;

    @NotBlank
    @JsonProperty(value = "sPassword")
    private String password;
}
