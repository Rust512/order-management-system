package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.converter.ProductAuditEntryToResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.domain.ProductAuditEntry;
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.ProductAuditEntryRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.TestSecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAuditEntryServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductAuditEntryRepository productAuditEntryRepository;
    @Mock
    private ProductAuditEntryToResponse productAuditEntryToResponse;

    @InjectMocks
    private ProductAuditEntryService productAuditEntryService;

    @BeforeEach
    void setUp() {
        TestSecurityUtils.setAuthenticationContext(1L, "U0", CommonConstants.ROLE_ADMIN);
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            In the createProductAuditEntry service, if a user with the given user ID does not exist,
            the method should throw a ResourceNotFoundException.
            """)
    void createProductAuditEntry_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        var userId = 1L;
        var operationType = OperationType.CREATE;
        var product = Product.builder()
                .id(1L)
                .name("Pr0")
                .price(BigDecimal.TEN)
                .stock(2L)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> productAuditEntryService.createProductAuditEntry(userId, product, operationType))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        ErrorMessageConstants.RESOURCE_NOT_FOUND,
                        CommonConstants.USER,
                        "id",
                        userId
                ));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(productAuditEntryRepository);
    }

    @Test
    @DisplayName(value = """
            In the createProductAuditEntry service, if a user with the given user ID exists,
            the method should save the product audit entry.
            """)
    void createProductAuditEntry_WhenUserExists_ShouldSaveAuditEntry() {
        var userId = 1L;
        var user = User.builder()
                .id(userId)
                .build();

        var productId = 1L;
        var productName = "Pr0";
        var productPrice = BigDecimal.TEN;
        var productStock = 2L;
        var nextVersion = 3L;
        var product = Product.builder()
                .id(productId)
                .name(productName)
                .price(productPrice)
                .stock(productStock)
                .build();
        var operationType = OperationType.UPDATE;

        var auditEntryId = 4L;
        var auditEntry = ProductAuditEntry.builder()
                .id(auditEntryId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productAuditEntryRepository.getNextAuditVersionByProductId(productId)).thenReturn(nextVersion);
        when(productAuditEntryRepository.save(any())).thenReturn(auditEntry);

        productAuditEntryService.createProductAuditEntry(userId, product, operationType);

        verify(userRepository).findById(userId);
        verify(productAuditEntryRepository).getNextAuditVersionByProductId(productId);

        var productAuditEntryCaptor = ArgumentCaptor.forClass(ProductAuditEntry.class);
        verify(productAuditEntryRepository).save(productAuditEntryCaptor.capture());

        var productAuditEntry = productAuditEntryCaptor.getValue();
        Assertions.assertThat(productAuditEntry.getVersion()).isEqualTo(nextVersion);
        Assertions.assertThat(productAuditEntry.getProductName()).isEqualTo(productName);
        Assertions.assertThat(productAuditEntry.getPrice()).isEqualTo(productPrice);
        Assertions.assertThat(productAuditEntry.getStock()).isEqualTo(productStock);
        Assertions.assertThat(productAuditEntry.getOperationType()).isEqualTo(operationType);
        Assertions.assertThat(productAuditEntry.getUser()).isEqualTo(user);
        Assertions.assertThat(productAuditEntry.getProduct()).isEqualTo(product);

        verifyNoMoreInteractions(userRepository, productAuditEntryRepository);
    }

    @Test
    @DisplayName(value = """
            In the getProductVersions service, if the given product ID does not exist,
            the method should throw a ResourceNotFoundException.
            """)
    void getProductVersions_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 1L;
        var pageable = PageRequest.of(0, 1);

        when(productRepository.existsById(productId)).thenReturn(false);

        Assertions.assertThatThrownBy(() -> productAuditEntryService.getProductVersions(productId, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        ErrorMessageConstants.RESOURCE_NOT_FOUND,
                        CommonConstants.PRODUCT,
                        "id",
                        productId
                ));

        verify(productRepository).existsById(productId);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(productAuditEntryRepository, productAuditEntryToResponse);
    }

    @Test
    @DisplayName(value = """
            In the getProductAuditEntryByVersion service,
            if an audit entry with the given product ID and version does not exist,
            the method should throw a ResourceNotFoundException.
            """)
    void getProductAuditEntryByVersion_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 1L;
        var version = 3L;

        when(productAuditEntryRepository.findByProduct_IdAndVersion(productId, version)).thenReturn(Optional.empty());

        var fields = "(productId, version)";
        var foundValues = String.format("(%d, %d)", productId, version);

        Assertions.assertThatThrownBy(() -> productAuditEntryService.getProductAuditEntryByVersion(productId, version))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        ErrorMessageConstants.RESOURCE_NOT_FOUND,
                        CommonConstants.PRODUCT_AUDIT_ENTRY,
                        fields,
                        foundValues
                ));

        verify(productAuditEntryRepository).findByProduct_IdAndVersion(productId, version);
        verifyNoMoreInteractions(productAuditEntryRepository);
        verifyNoInteractions(userRepository, productRepository, productAuditEntryToResponse);
    }
}