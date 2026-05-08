package com.design.order_management_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotNull
    @JsonProperty(value = "sProductId")
    private Long productId;

    @NotBlank
    @JsonProperty(value = "sProductName")
    private String productName;

    @Positive
    @JsonProperty(value = "dPrice")
    private BigDecimal price;

    @PositiveOrZero
    @JsonProperty(value = "dStock")
    private Long stock;
}
