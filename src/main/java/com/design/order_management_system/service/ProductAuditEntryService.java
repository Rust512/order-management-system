package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.ProductAuditEntryToResponse;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductAuditEntryResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.domain.ProductAuditEntry;
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.repository.ProductAuditEntryRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAuditEntryService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductAuditEntryRepository productAuditEntryRepository;
    private final ProductAuditEntryToResponse productAuditEntryToResponse;

    @Transactional
    void createProductAuditEntry(Long userId, Product product, OperationType operationType) {
        var productId = product.getId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("product audit entry creation failed; userId={} productId={} reason=user_not_found", userId, productId);
                    return new ResourceNotFoundException(CommonConstants.USER, "id", String.valueOf(userId));
                });

        var nextVersion = productAuditEntryRepository.getNextAuditVersionByProductId(product.getId());

        var productAuditEntry = ProductAuditEntry.builder()
                .version(nextVersion)
                .productName(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .operationType(operationType)
                .user(user)
                .product(product)
                .build();

        var savedAuditEntry = productAuditEntryRepository.save(productAuditEntry);
        log.info("Product audit entry created; userId={} productId={} auditEntryId={}", userId, productId, savedAuditEntry.getId());
    }

    public PagedResponse<ProductAuditEntryResponse> getProductVersions(Long productId, Pageable pageable) {
        var userId = SecurityUtils.getPrincipalUser().getUserId();
        if (!productRepository.existsById(productId)) {
            log.warn("Product audit entry fetch failed; userId={} productId={} reason=product_not_found", userId, productId);
            throw new ResourceNotFoundException(CommonConstants.PRODUCT, "id", String.valueOf(productId));
        }
        var result = productAuditEntryRepository.findByProduct_Id(productId, pageable);

        var auditEntries = result.getContent()
                .stream()
                .map(productAuditEntryToResponse)
                .toList();

        return PagedResponse.<ProductAuditEntryResponse>builder()
                .content(auditEntries)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public ProductAuditEntryResponse getProductAuditEntryByVersion(Long productId, Long version) {
        var userId = SecurityUtils.getPrincipalUser().getUserId();
        return productAuditEntryRepository.findByProduct_IdAndVersion(productId, version)
                .map(productAuditEntryToResponse)
                .orElseThrow(() -> {
                    log.warn("Product audit entry fetch failed; userId={} productId={} version={} reason=audit_entry_not_found", userId, productId, version);
                    return new ResourceNotFoundException(CommonConstants.PRODUCT_AUDIT_ENTRY, "(productId, version)", String.format("(%d, %d)", productId, version));
                });
    }
}
