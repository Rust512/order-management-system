package com.design.order_management_system.service;

import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.OrderItemSnapshot;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderItemSnapshotService implements Function<OrderItem, OrderItemSnapshot> {

  @Override
  @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
  public OrderItemSnapshot apply(OrderItem orderItem) {
    var product = orderItem.getProduct();

    return OrderItemSnapshot.builder()
        .productId(product.getId())
        .productName(product.getName())
        .quantity(orderItem.getQuantity())
        .purchasePrice(orderItem.getPurchasePrice())
        .build();
  }
}
