package com.design.order_management_system.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProductRequest {

    @NotBlank
    @JsonProperty(value = "sProductName")
    private String productName;

    @NotNull
    @Positive
    @JsonProperty(value = "dPrice")
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @JsonProperty(value = "dStock")
    private Long stock;
}
