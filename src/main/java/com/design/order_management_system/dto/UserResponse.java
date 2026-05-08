package com.design.order_management_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    @JsonProperty(value = "sUsername")
    private String username;

    @JsonProperty(value = "aRoles")
    private List<String> roles;
}
