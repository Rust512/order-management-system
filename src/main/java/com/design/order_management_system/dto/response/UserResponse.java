package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Builder
@Validated
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    @JsonProperty(value = "sUsername")
    private String username;

    @NotEmpty
    @JsonProperty(value = "aRoles")
    private List<String> roles;
}
