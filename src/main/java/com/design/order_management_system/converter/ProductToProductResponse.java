package com.design.order_management_system.converter;

import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.model.domain.Product;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ProductToProductResponse implements Function<Product, ProductResponse> {
    @Override
    public ProductResponse apply(Product source) {
        return ProductResponse.builder()
                .productId(source.getId())
                .productName(source.getName())
                .price(source.getPrice())
                .stock(source.getStock())
                .build();
    }
}
