package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateProductRequestToProduct;
import com.design.order_management_system.converter.ProductToProductResponse;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CreateProductRequestToProduct createProductRequestToProduct;
    private final ProductToProductResponse productToProductResponse;

    public ProductResponse registerProduct(CreateProductRequest createProductRequest) {
        String productName = createProductRequest.getProductName();
        if (productRepository.existsByName(productName)) {
            throw new DuplicateResourceException(CommonConstants.PRODUCT, CommonConstants.NAME, productName);
        }

        Product product = productRepository.save(createProductRequestToProduct.apply(createProductRequest));

        return productToProductResponse.apply(product);
    }
}
