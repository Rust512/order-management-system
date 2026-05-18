package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.domain.ProductAuditEntry;
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.repository.ProductAuditEntryRepository;
import com.design.order_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAuditEntryService {
    private final UserRepository userRepository;
    private final ProductAuditEntryRepository productAuditEntryRepository;

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
}
