package com.design.order_management_system.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemRequest {

    @NotNull
    @Positive
    @JsonProperty(value = "dProductId")
    private Long productId;

    @NotNull
    @Positive
    @JsonProperty(value = "dQuantity")
    private Long quantity;
}
