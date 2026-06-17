package com.design.order_management_system.controller;

import com.design.order_management_system.documentation.annotation.BadRequest;
import com.design.order_management_system.documentation.annotation.InsufficientResourcesResponse;
import com.design.order_management_system.documentation.examples.ErrorResponseExamples;
import com.design.order_management_system.documentation.examples.RequestExamples;
import com.design.order_management_system.documentation.examples.ResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/orders/items")
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping
    @Operation(
            summary = "Add order item",
            description = """
                    This API adds an order item to the draft order corresponding to the logged in user.
                    If none of the existing items correspond to the provided product ID, a new item entry is added.
                    If an order item with the provided product ID exists,
                    the quantity is increased (old quantity + provided quantity) and price are updated.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = RequestExamples.ORDER_REQUEST)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order checkout successful",
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
    @BadRequest
    @InsufficientResourcesResponse
    ResponseEntity<OrderResponse> addOrderItem(@Valid @RequestBody OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.addOrderItem(orderItemRequest));
    }

    @PutMapping
    @Operation(
            summary = "Update order item",
            description = """
                    This API updates the product price and quantity of the order item corresponding to the
                    provided product ID in the draft order corresponding to the logged-in user.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = RequestExamples.ORDER_REQUEST)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order checkout successful",
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
    @BadRequest
    @InsufficientResourcesResponse
    ResponseEntity<OrderResponse> editOrderItem(@Valid @RequestBody OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.editOrderItem(orderItemRequest));
    }

    @DeleteMapping(path = "/{productId}")
    @Operation(
            summary = "Remove order item",
            description = """
                    This API removes the item corresponding to the provided product ID
                    in the draft order corresponding to the logged-in user.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order checkout successful",
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
    ResponseEntity<OrderResponse> deleteOrderItem(@PathVariable Long productId) {
        return ResponseEntity.ok(orderItemService.removeOrderItem(productId));
    }
}
