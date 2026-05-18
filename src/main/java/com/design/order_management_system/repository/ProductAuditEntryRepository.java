package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.ProductAuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductAuditEntryRepository extends JpaRepository<ProductAuditEntry, Long> {
    @Query(value = """
            SELECT COALESCE(MAX(pae.version), 0) + 1
            FROM ProductAuditEntry pae
            WHERE pae.product.id = :productId
            """)
    Long getNextAuditVersionByProductId(Long productId);

    @EntityGraph(attributePaths = "user")
    Page<ProductAuditEntry> findByProduct_Id(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Optional<ProductAuditEntry> findByProduct_IdAndVersion(Long productId, Long version);
}
