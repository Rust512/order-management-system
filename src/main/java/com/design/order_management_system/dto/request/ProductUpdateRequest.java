package com.design.order_management_system.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ProductUpdateRequest {
    @JsonProperty(value = "sNewProductName")
    private String newProductName;

    @Positive
    @JsonProperty(value = "dUpdatedPrice")
    private BigDecimal updatedPrice;

    @JsonProperty(value = "dStockToAdd")
    private Long stockToAdd;
}
