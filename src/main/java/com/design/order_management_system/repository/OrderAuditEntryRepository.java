package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.OrderAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAuditEntryRepository extends JpaRepository<OrderAuditEntry, Long> {
}
