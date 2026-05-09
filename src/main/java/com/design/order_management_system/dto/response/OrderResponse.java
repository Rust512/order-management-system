package com.design.order_management_system.dto.response;

import com.design.order_management_system.model.enumeration.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
    @JsonProperty(value = "dOrderId")
    private Long orderId;

    @JsonProperty(value = "sOrderStatus")
    private OrderStatus orderStatus;

    @JsonProperty(value = "dtCreatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonProperty(value = "dTotalPrice")
    private BigDecimal totalPrice;

    @JsonProperty(value = "aOrderItems")
    private List<OrderItemResponse> orderItems;
}
