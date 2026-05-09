package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class OrderItemResponse {
    @JsonProperty(value = "sProductName")
    private String productName;

    @JsonProperty(value = "dQuantity")
    private Long quantity;

    @JsonProperty(value = "dPurchasePrice")
    private BigDecimal purchasePrice;
}
