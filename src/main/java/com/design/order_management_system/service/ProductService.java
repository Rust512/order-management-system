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
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductAuditEntryService productAuditEntryService;
    private final CreateProductRequestToProduct createProductRequestToProduct;
    private final ProductToProductResponse productToProductResponse;

    @Transactional
    public ProductResponse registerProduct(CreateProductRequest createProductRequest) {
        Long userId = SecurityUtils.getPrincipalUser().getUserId();
        String productName = createProductRequest.getProductName();
        log.info("Product registration requested; userId={} productName={}", userId, productName);

        if (productRepository.existsByName(productName)) {
            log.warn("Product registration failed; userId={} productName={} reason=product_already_exists", userId, productName);
            throw new DuplicateResourceException(CommonConstants.PRODUCT, "name", productName);
        }

        Product product = productRepository.save(createProductRequestToProduct.apply(createProductRequest));

        productAuditEntryService.saveProductAuditEntry(userId, product, OperationType.CREATE);

        log.info("Product registered; userId={} productId={} productName={}", userId, product.getId(), productName);

        return productToProductResponse.apply(product);
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest productUpdateRequest) {
        Long userId = SecurityUtils.getPrincipalUser().getUserId();
        log.info("Product update requested; userId={} productId={}", userId, id);

        var product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product update failed; userId={} productId={} reason=product_not_found", userId, id);
                    return new ResourceNotFoundException(
                            CommonConstants.PRODUCT,
                            "id",
                            String.valueOf(id)
                    );
                });

        String newProductName = productUpdateRequest.getNewProductName();
        if (newProductName != null && !newProductName.equalsIgnoreCase(product.getName())) {
            product.setName(newProductName);
        }

        BigDecimal updatedPrice = productUpdateRequest.getUpdatedPrice();
        if (updatedPrice != null && updatedPrice.compareTo(product.getPrice()) != 0) {
            product.setPrice(updatedPrice);
        }

        if (productUpdateRequest.getStockToAdd() != null) {
            Long updatedStock = product.getStock() + productUpdateRequest.getStockToAdd();
            if (updatedStock.compareTo(0L) < 0) {
                log.warn("Update product failed; userId={} productId={} reason=stock_cannot_be_negative", userId, id);
                throw new IllegalArgumentException(ErrorMessageConstants.PRODUCT_STOCK_CANNOT_BE_NEGATIVE);
            }
            product.setStock(updatedStock);
        }

        var savedProduct = productRepository.save(product);

        log.info("Product updated; userId={} productId={}", userId, id);

        productAuditEntryService.saveProductAuditEntry(userId, product, OperationType.UPDATE);

        return productToProductResponse.apply(savedProduct);
    }
}
