package com.design.order_management_system.converter;

import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.model.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class OrderToOrderResponse implements Function<Order, OrderResponse> {
    private final OrderItemToOrderItemResponse orderItemToOrderItemResponse;

    @Override
    public OrderResponse apply(Order order) {
        var orderItemList = order.getOrderItems()
                .stream()
                .map(orderItemToOrderItemResponse)
                .toList();

        var totalPrice = orderItemList.stream()
                .map(orderItem -> orderItem.getPurchasePrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .totalPrice(totalPrice)
                .orderItems(orderItemList)
                .build();
    }
}
