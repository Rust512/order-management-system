package com.design.order_management_system.model.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSnapshot {
    private Long productId;
    private String productName;
    private Long quantity;
    private BigDecimal purchasePrice;
}
