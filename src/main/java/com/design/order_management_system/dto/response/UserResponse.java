package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    @JsonProperty(value = "sUsername")
    private String username;

    @JsonProperty(value = "aRoles")
    private List<String> roles;
}
