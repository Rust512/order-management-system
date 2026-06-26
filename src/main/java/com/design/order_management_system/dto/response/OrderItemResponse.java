package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
