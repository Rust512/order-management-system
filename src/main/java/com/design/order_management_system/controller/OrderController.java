package com.design.order_management_system.controller;

import com.design.order_management_system.documentation.examples.ErrorResponseExamples;
import com.design.order_management_system.documentation.examples.RequestExamples;
import com.design.order_management_system.documentation.examples.ResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.OrderRequest;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
            summary = "Register an order",
            description = "Register an order.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = RequestExamples.REGISTER_ORDER)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order registration successful",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponse.class),
                                    examples = @ExampleObject(value = ResponseExamples.REGISTER_ORDER)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = ErrorResponseExamples.ORDER_NOT_FOUND)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Insufficient product stock",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = ErrorResponseExamples.INSUFFICIENT_RESOURCES)
                            )
                    )
            }
    )
    ResponseEntity<OrderResponse> registerOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.registerOrder(orderRequest));
    }

    @GetMapping(path = "/{id}")
    @Operation(
            summary = "Fetch order by ID",
            description = """
                    Fetch an order by ID.
                    If the user has the ADMIN authority, they can fetch any order.
                    If the user does not have the ADMIN authority, the order will be fetched only if
                    the user owns the order.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order fetch successful",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponse.class),
                                    examples = @ExampleObject(value = ResponseExamples.REGISTER_ORDER)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = ErrorResponseExamples.ORDER_NOT_FOUND)
                            )
                    )
            }
    )
    ResponseEntity<OrderResponse> getOrder(
            @PathVariable
            @Parameter(
                    description = "The order ID",
                    example = "2"
            )
            Long id
    ) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
