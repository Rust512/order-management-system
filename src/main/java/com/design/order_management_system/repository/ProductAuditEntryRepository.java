package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.ProductAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAuditEntryRepository extends JpaRepository<ProductAuditEntry, Long> {
}
