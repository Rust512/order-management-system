package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
