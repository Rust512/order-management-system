package com.design.order_management_system.service;

import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.domain.ProductAuditEntry;
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.repository.ProductAuditEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAuditEntryService {
    private final ProductAuditEntryRepository productAuditEntryRepository;
    private final UserService userService;

    @Transactional
    void saveProductAuditEntry(Long userId, Product product, OperationType operationType) {
        var user = userService.getUserById(userId);
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
        log.info("Audit entry saved; userId={} productId={} auditEntryId={}", userId, product.getId(), savedAuditEntry.getId());
    }
}
