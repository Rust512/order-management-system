package com.design.order_management_system.converter;

import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.model.domain.Product;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CreateProductRequestToProduct implements Function<CreateProductRequest, Product> {
    @Override
    public Product apply(CreateProductRequest createProductRequest) {
        return Product.builder()
                .name(createProductRequest.getProductName())
                .price(createProductRequest.getPrice())
                .stock(createProductRequest.getStock())
                .build();
    }
}
