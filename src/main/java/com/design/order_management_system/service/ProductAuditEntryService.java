package com.design.order_management_system.service;

import com.design.order_management_system.model.domain.ProductAuditEntry;
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

    @Transactional
    void saveProductAuditEntry(Long userId, Long productId, ProductAuditEntry productAuditEntry) {
        var savedAuditEntry = productAuditEntryRepository.save(productAuditEntry);
        log.info("Audit entry saved; userId={} productId={} auditEntryId={}", userId, productId, savedAuditEntry.getId());
    }
}
