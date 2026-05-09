package com.design.order_management_system.controller;

import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ResponseEntity<ProductResponse> registerProduct(@RequestBody @Valid CreateProductRequest createProductRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.registerProduct(createProductRequest));
    }

    @GetMapping
    ResponseEntity<PagedResponse<ProductResponse>> getProducts(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(productService.getProducts(page, size));
    }

    @PutMapping(path = "/{id}")
    ResponseEntity<ProductResponse> updateProduct(@PathVariable long id, @RequestBody @Valid ProductUpdateRequest updateRequest) {
        return ResponseEntity.ok(productService.updateProduct(id, updateRequest));
    }
}
