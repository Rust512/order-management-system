package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    @JsonProperty(value = "sUsername")
    private String username;

    @JsonProperty(value = "aRoles")
    private List<String> roles;
}
