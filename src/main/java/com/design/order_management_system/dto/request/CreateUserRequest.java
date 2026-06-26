package com.design.order_management_system.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserRequest {
    @NotBlank
    @JsonProperty(value = "sUsername")
    private String username;

    @NotBlank
    @JsonProperty(value = "sPassword")
    private String password;
}
