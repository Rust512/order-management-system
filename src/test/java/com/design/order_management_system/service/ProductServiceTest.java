package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.converter.CreateProductRequestToProduct;
import com.design.order_management_system.converter.ProductToProductResponse;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CreateProductRequestToProduct createProductRequestToProduct;
    @Mock
    private ProductToProductResponse productToProductResponse;
    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName(value = """
            When the product registration service receives an already existing product name,
            it should throw a DuplicateResourceException
            """)
    void registerProduct_WhenProductNameAlreadyExists_ShouldThrowDuplicateResourceException() {
        String productName = "P0";
        var request = CreateProductRequest.builder()
                .productName(productName)
                .price(BigDecimal.valueOf(1))
                .stock(1L)
                .build();

        when(productRepository.existsByName(productName)).thenReturn(true);

        Assertions.assertThatThrownBy(() -> productService.registerProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(String.format(
                        DuplicateResourceException.ALREADY_EXISTS,
                        CommonConstants.PRODUCT,
                        CommonConstants.NAME,
                        productName)
                );

        verify(productRepository).existsByName(productName);
        verify(productRepository, never()).save(any());
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(createProductRequestToProduct, productToProductResponse);
    }

    @Test
    @DisplayName(value = """
            When the stockToAdd parameter in the update product service request body
            is less than the negated current stock of the corresponding product;
            i.e. updatedStock = stockToUpdate + currentStock < 0
            the service should throw an IllegalArgumentException.
            """)
    void updateProduct_WhenStockToAddLessThanNegatedCurrentStock_ShouldThrowIllegalArgumentException() {
        Long productId = 1L;
        Long stock = 2L;
        String productName = "P0";
        BigDecimal price = BigDecimal.ONE;

        var product = Product.builder()
                .id(productId)
                .name(productName)
                .price(price)
                .stock(stock)
                .build();

        // -stock - 1 is always less than -stock 
        var updateProductRequest = ProductUpdateRequest.builder()
                .newProductName(productName)
                .updatedPrice(BigDecimal.ONE)
                .stockToAdd(-stock - 1)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Assertions.assertThatThrownBy(() -> productService.updateProduct(productId, updateProductRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessageConstants.PRODUCT_STOCK_CANNOT_BE_NEGATIVE);

        Assertions.assertThat(product.getStock()).isEqualTo(stock);

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productToProductResponse);
    }
}