package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    @JsonProperty(value = "sProductId")
    private Long productId;

    @JsonProperty(value = "sProductName")
    private String productName;

    @JsonProperty(value = "dPrice")
    private BigDecimal price;

    @JsonProperty(value = "dStock")
    private Long stock;
}
