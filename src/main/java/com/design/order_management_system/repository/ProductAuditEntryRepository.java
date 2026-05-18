package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.ProductAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductAuditEntryRepository extends JpaRepository<ProductAuditEntry, Long> {
    @Query(value = """
            SELECT COALESCE(MAX(pae.version), 0) + 1
            FROM ProductAuditEntry pae
            WHERE pae.product.id = :productId
            """)
    Long getNextAuditVersionByProductId(Long productId);
}
