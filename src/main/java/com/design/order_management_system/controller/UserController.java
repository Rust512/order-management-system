package com.design.order_management_system.controller;

import com.design.order_management_system.constants.swagger.SwaggerErrorResponseExamples;
import com.design.order_management_system.constants.swagger.SwaggerRequestExamples;
import com.design.order_management_system.constants.swagger.SwaggerResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(
            summary = "Register a user",
            description = """
                    Register a new user account.
                    Only Admin users are authorized to create users.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = SwaggerRequestExamples.USER_REGISTRATION)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Registration successful",
                            content = @Content(
                                    schema = @Schema(implementation = UserResponse.class),
                                    examples = @ExampleObject(value = SwaggerResponseExamples.REGISTER_USER)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "User already exists",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = SwaggerErrorResponseExamples.USER_ALREADY_EXISTS)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = SwaggerErrorResponseExamples.INVALID_TOKEN)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Admin role required",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = SwaggerErrorResponseExamples.ACCESS_DENIED)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = SwaggerErrorResponseExamples.BAD_REQUEST)
                            )
                    )
            }
    )
    ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.userRegistration(createUserRequest));
    }

    @GetMapping(path = "/{id}")
    ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
