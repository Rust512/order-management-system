package com.design.order_management_system.documentation.annotation;

import com.design.order_management_system.documentation.examples.ErrorResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@ApiResponse(
        responseCode = "400",
        description = "Insufficient resources",
        content = @Content(
                schema = @Schema(implementation = ApiErrorResponse.class),
                examples = @ExampleObject(value = ErrorResponseExamples.INSUFFICIENT_RESOURCES)
        )
)
public @interface InsufficientResourcesResponse {
}
