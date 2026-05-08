package com.design.order_management_system.dto.response;

import com.design.order_management_system.model.enumeration.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@Validated
@AllArgsConstructor
public class OrderResponse {

    @NotNull
    @Positive
    @JsonProperty(value = "dOrderId")
    private Long orderId;

    @NotNull
    @JsonProperty(value = "sOrderStatus")
    private OrderStatus orderStatus;

    @NotNull
    @JsonProperty(value = "dtCreatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonProperty(value = "dTotalPrice")
    private BigDecimal totalPrice;

    @NotEmpty
    @JsonProperty(value = "aOrderItems")
    private List<OrderItemResponse> orderItems;
}
