package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Data
@Builder
@Validated
@AllArgsConstructor
public class OrderItemResponse {
    @NotBlank
    @JsonProperty(value = "sProductName")
    private String productName;

    @NotNull
    @Positive
    @JsonProperty(value = "dQuantity")
    private Long quantity;

    @NotNull
    @Positive
    @JsonProperty(value = "dPurchasePrice")
    private BigDecimal purchasePrice;
}
