package com.design.order_management_system.controller;

import com.design.order_management_system.constants.swagger.SwaggerRequestExamples;
import com.design.order_management_system.constants.swagger.SwaggerResponseExamples;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token generation")
public class LoginController {

    private final LoginService loginService;

    @PostMapping(path = "/login")
    @SecurityRequirement(name = "none")
    @Operation(
            summary = "Generate JWT Token",
            description = """
                    Submit user credentials to generate a bearer token.
                    Use the provided seeded user credentials for testing.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = SwaggerRequestExamples.LOGIN))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(examples = @ExampleObject(value = SwaggerResponseExamples.LOGIN))
                    ),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(loginService.getToken(loginRequest));
    }
}
