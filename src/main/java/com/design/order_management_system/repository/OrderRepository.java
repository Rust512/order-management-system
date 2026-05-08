package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = """
            SELECT o
            FROM Order o
            WHERE o.id = :id
            """)
    @EntityGraph(attributePaths = {
            "orderItems.product.name",
            "orderItems.quantity",
            "orderItems.purchasePrice"
    })
    Optional<Order> getOrderByIdWithItems(Long id);
}
