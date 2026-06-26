package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(
            value =
                    """
            SELECT o
            FROM Order o
            WHERE o.id = :id
            """)
    @EntityGraph(
            attributePaths = {
                "orderItems",
                "orderItems.product",
            })
    Optional<Order> getOrderByIdWithItems(Long id);

    @Query(
            value =
                    """
            SELECT o
            FROM Order o
            INNER JOIN o.user user
            WHERE o.id = :orderId
            AND user.id = :userId
            """)
    @EntityGraph(
            attributePaths = {
                "orderItems",
                "orderItems.product",
            })
    Optional<Order> getOrderByIdAndUserIdWithItems(Long orderId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            value =
                    """
            SELECT o
            FROM Order o
            INNER JOIN o.user u
            WHERE u.id = :userId
            AND o.orderStatus = :orderStatus
            """)
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> fetchDraftOrderWithOrderItems(Long userId, OrderStatus orderStatus);
}
