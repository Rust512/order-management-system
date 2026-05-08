package com.design.order_management_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    @NotBlank
    @JsonProperty(value = "sProductName")
    private String productName;

    @Positive
    @JsonProperty(value = "dPrice")
    private BigDecimal price;

    @Min(value = 0L)
    @JsonProperty(value = "dStock")
    private Long stock;
}
