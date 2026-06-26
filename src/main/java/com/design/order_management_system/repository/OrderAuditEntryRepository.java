package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.OrderAuditEntry;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderAuditEntryRepository extends JpaRepository<OrderAuditEntry, Long> {
  @Query(
      value =
          """
            SELECT COALESCE(MAX(oae.version), 0) + 1
            FROM OrderAuditEntry oae
            WHERE oae.order.id = :orderId
            """)
  Long getNextAuditVersionByOrderId(Long orderId);

  @EntityGraph(attributePaths = {"user"})
  Page<OrderAuditEntry> findByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<OrderAuditEntry> findByOrderId(Long orderId, Pageable pageable);

  boolean existsByOrderIdAndVersion(Long orderId, Long version);

  @EntityGraph(attributePaths = {"user"})
  Optional<OrderAuditEntry> findByOrderIdAndVersion(Long orderId, Long version);

  @EntityGraph(attributePaths = {"user"})
  Optional<OrderAuditEntry> findByOrderIdAndUserIdAndVersion(
      Long orderId, Long userId, Long version);
}
