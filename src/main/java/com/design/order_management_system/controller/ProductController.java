package com.design.order_management_system.controller;

import com.design.order_management_system.constants.swagger.SwaggerRequestExamples;
import com.design.order_management_system.constants.swagger.SwaggerResponseExamples;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(
            summary = "Register a product",
            description = """
                    Register a new product.
                    Only Admin users are authorized to create products.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = SwaggerRequestExamples.PRODUCT_REGISTRATION))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Registration successful",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Admin role required"
                    )
            }
    )
    ResponseEntity<ProductResponse> registerProduct(@RequestBody @Valid CreateProductRequest createProductRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.registerProduct(createProductRequest));
    }

    @GetMapping
    ResponseEntity<PagedResponse<ProductResponse>> getProducts(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(productService.getProducts(page, size));
    }

    @PutMapping(path = "/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(
            summary = "Update a product",
            description = """
                    Update a new product.
                    Only Admin users are authorized to update products.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = SwaggerRequestExamples.UPDATE_PRODUCT))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Update successful",
                            content = @Content(
                                    schema = @Schema(implementation = ProductResponse.class),
                                    examples = @ExampleObject(value = SwaggerResponseExamples.REGISTER_PRODUCT)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = SwaggerResponseExamples.INVALID_TOKEN)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Admin role required",
                            content = @Content(
                                    schema = @Schema(implementation = ApiErrorResponse.class),
                                    examples = @ExampleObject(value = SwaggerResponseExamples.ACCESS_DENIED)
                            )
                    )
            }
    )
    ResponseEntity<ProductResponse> updateProduct(@PathVariable long id, @RequestBody @Valid ProductUpdateRequest updateRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, updateRequest));
    }
}
