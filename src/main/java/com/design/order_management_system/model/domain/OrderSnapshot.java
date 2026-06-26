package com.design.order_management_system.model.domain;

import com.design.order_management_system.model.enumeration.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSnapshot {
  private Long orderId;
  private OrderStatus orderStatus;
  private BigDecimal totalPrice;
  private List<OrderItemSnapshot> orderItemSnapshots;
}
