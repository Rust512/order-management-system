package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.OrderItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT orderItem
            FROM OrderItem orderItem
            WHERE orderItem.product.id = :productId AND orderItem.order.id = :orderId
            """)
    Optional<OrderItem> findByOrder_IdAndProduct_IdForUpdate(Long orderId, Long productId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(value = """
            SELECT orderItem
            FROM OrderItem orderItem
            WHERE orderItem.order.id = :orderId
            ORDER BY orderItem.id ASC
            """)
    @EntityGraph(attributePaths = "product")
    List<OrderItem> findAllByOrder_IdForRead(Long orderId);
}
