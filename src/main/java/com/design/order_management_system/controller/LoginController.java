package com.design.order_management_system.controller;

import com.design.order_management_system.documentation.examples.ErrorResponseExamples;
import com.design.order_management_system.documentation.examples.RequestExamples;
import com.design.order_management_system.documentation.examples.ResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @SecurityRequirements
    @Operation(
            summary = "Generate JWT Token",
            description = """
                    Submit user credentials to generate a bearer token.
                    Use the provided seeded user credentials for testing.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = RequestExamples.LOGIN)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(
                                    schema = @Schema(implementation = LoginResponse.class),
                                    examples = @ExampleObject(value = ResponseExamples.LOGIN)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = ErrorResponseExamples.INVALID_CREDENTIALS)
                            )
                    )
            }
    )
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(loginService.getToken(loginRequest));
    }
}
