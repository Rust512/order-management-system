package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.converter.CreateProductRequestToProduct;
import com.design.order_management_system.converter.ProductToProductResponse;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public PagedResponse<ProductResponse> getProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);

        var productResponseList = productPage.getContent()
                .stream()
                .map(productToProductResponse)
                .toList();

        return PagedResponse.<ProductResponse>builder()
                .content(productResponseList)
                .page(page)
                .size(size)
                .totalElements(productRepository.count())
                .totalPages(productPage.getTotalPages())
                .build();
    }

    public ProductResponse updateProduct(Long id, ProductUpdateRequest productUpdateRequest) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CommonConstants.PRODUCT,
                        CommonConstants.ID,
                        String.valueOf(id)
                ));

        if (productUpdateRequest.getNewProductName() != null) {
            product.setName(productUpdateRequest.getNewProductName());
        }

        if (productUpdateRequest.getUpdatedPrice() != null) {
            product.setPrice(productUpdateRequest.getUpdatedPrice());
        }

        if (productUpdateRequest.getStockToAdd() != null) {
            Long updatedStock = product.getStock() + productUpdateRequest.getStockToAdd();
            if (updatedStock.compareTo(0L) < 0) {
                throw new IllegalArgumentException(ErrorMessageConstants.PRODUCT_STOCK_CANNOT_BE_NEGATIVE);
            }
            product.setStock(updatedStock);
        }

        return productToProductResponse.apply(productRepository.save(product));
    }
}
