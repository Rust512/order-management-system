package com.design.order_management_system.converter;

import com.design.order_management_system.dto.response.OrderItemResponse;
import com.design.order_management_system.model.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrderItemToOrderItemResponse implements Function<OrderItem, OrderItemResponse> {
    @Override
    public OrderItemResponse apply(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .productName(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .purchasePrice(orderItem.getPurchasePrice())
                .build();
    }
}
