package com.design.order_management_system.documentation.annotation;

import com.design.order_management_system.documentation.examples.ErrorResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "400",
          description = "Validation failed",
          content =
              @Content(
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(value = ErrorResponseExamples.BAD_REQUEST))),
      @ApiResponse(
          responseCode = "401",
          description = "Authentication required",
          content =
              @Content(
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(value = ErrorResponseExamples.INVALID_TOKEN))),
      @ApiResponse(
          responseCode = "403",
          description = "Admin role required",
          content =
              @Content(
                  schema = @Schema(implementation = ApiErrorResponse.class),
                  examples = @ExampleObject(value = ErrorResponseExamples.ACCESS_DENIED)))
    })
public @interface AdminErrorResponses {}
