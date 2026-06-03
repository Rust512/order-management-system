package com.design.order_management_system.controller;

import com.design.order_management_system.documentation.annotation.AdminErrorResponses;
import com.design.order_management_system.documentation.annotation.BadRequest;
import com.design.order_management_system.documentation.examples.ErrorResponseExamples;
import com.design.order_management_system.documentation.examples.RequestExamples;
import com.design.order_management_system.documentation.examples.ResponseExamples;
import com.design.order_management_system.documentation.schema.PagedProductAuditEntryResponse;
import com.design.order_management_system.documentation.schema.PagedProductResponse;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductAuditEntryResponse;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.service.ProductAuditEntryService;
import com.design.order_management_system.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final ProductAuditEntryService productAuditEntryService;

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    @AdminErrorResponses
    @Operation(
            summary = "Register a product",
            description = """
                    Register a new product.
                    Only Admin users are authorized to create products.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = RequestExamples.PRODUCT_REGISTRATION)
                    )
            )
    )
    @ApiResponse(
            responseCode = "201",
            description = "Registration successful",
            content = @Content(
                    schema = @Schema(implementation = ProductResponse.class),
                    examples = @ExampleObject(value = ResponseExamples.REGISTER_PRODUCT)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Product already exists",
            content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class),
                    examples = @ExampleObject(value = ErrorResponseExamples.PRODUCT_ALREADY_EXISTS)
            )
    )
    ResponseEntity<ProductResponse> registerProduct(@RequestBody @Valid CreateProductRequest createProductRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.registerProduct(createProductRequest));
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @AdminErrorResponses
    @PutMapping(path = "/{id}")
    @Operation(
            summary = "Update a product",
            description = """
                    Update a new product.
                    Only Admin users are authorized to update products.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = RequestExamples.UPDATE_PRODUCT)
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Update successful",
            content = @Content(
                    schema = @Schema(implementation = ProductResponse.class),
                    examples = @ExampleObject(value = ResponseExamples.UPDATE_PRODUCT)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class),
                    examples = @ExampleObject(value = ErrorResponseExamples.PRODUCT_NOT_FOUND)
            )
    )
    ResponseEntity<ProductResponse> updateProduct(@PathVariable long id, @RequestBody @Valid ProductUpdateRequest updateRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, updateRequest));
    }

    @GetMapping
    @Operation(
            summary = "get products in pages",
            description = """
                    Fetch all products in pages.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fetch successful",
            content = @Content(
                    schema = @Schema(implementation = PagedProductResponse.class),
                    examples = @ExampleObject(value = ResponseExamples.GET_PRODUCTS)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Authentication required",
            content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class),
                    examples = @ExampleObject(value = ErrorResponseExamples.INVALID_TOKEN)
            )
    )
    @BadRequest
    ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @RequestParam
            @Parameter(
                    description = "Zero-based page index",
                    example = "0"
            )
            int page,

            @RequestParam
            @Positive
            @Parameter(
                    description = "The number of items per page",
                    example = "5"
            )
            int size
    ) {
        return ResponseEntity.ok(productService.getProducts(page, size));
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @AdminErrorResponses
    @GetMapping(path = "/{productId}/audit")
    @Operation(
            summary = "get product audit entries by product ID in pages",
            description = """
                    Fetch product audit entries by product ID in pages.
                    Only Admin users are authorized to fetch product audit entries.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fetch successful",
            content = @Content(
                    schema = @Schema(implementation = PagedProductAuditEntryResponse.class),
                    examples = @ExampleObject(value = ResponseExamples.GET_PRODUCT_AUDIT_ENTRIES)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class),
                    examples = @ExampleObject(value = ErrorResponseExamples.PRODUCT_NOT_FOUND_FOR_AUDIT)
            )
    )
    ResponseEntity<PagedResponse<ProductAuditEntryResponse>> getProductAuditEntries(
            @Positive
            @PathVariable
            @Parameter(
                    description = "The product ID",
                    example = "2"
            )
            long productId,

            @ParameterObject
            @PageableDefault(size = 5, sort = "version", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(productAuditEntryService.getProductVersions(productId, pageable));
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @AdminErrorResponses
    @GetMapping(path = "/{productId}/audit/{version}")
    @Operation(
            summary = "get product audit entry by product ID and version",
            description = """
                    Fetch product audit entry by product ID and version.
                    Only Admin users are authorized to fetch a product audit entry.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fetch successful",
            content = @Content(
                    schema = @Schema(implementation = PagedResponse.class),
                    examples = @ExampleObject(value = ResponseExamples.GET_PRODUCT_AUDIT_ENTRY)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class),
                    examples = @ExampleObject(value = ErrorResponseExamples.PRODUCT_AUDIT_ENTRY_NOT_FOUND)
            )
    )
    ResponseEntity<ProductAuditEntryResponse> getProductAuditEntry(
            @Positive
            @PathVariable
            @Parameter(
                    description = "The product ID",
                    example = "2"
            )
            long productId,

            @Positive
            @PathVariable
            @Parameter(
                    description = "The product audit entry version",
                    example = "3"
            )
            long version
    ) {
        return ResponseEntity.ok(productAuditEntryService.getProductAuditEntryByVersion(productId, version));
    }
}
