package com.design.order_management_system.dto.response;

import com.design.order_management_system.model.enumeration.OperationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record ProductAuditEntryResponse(
        @JsonProperty(value = "dVersion")
        Long version,

        @JsonProperty(value = "sProductName")
        String productName,

        @JsonProperty(value = "dPrice")
        BigDecimal price,

        @JsonProperty(value = "dStock")
        Long stock,

        @JsonProperty(value = "sOperationType")
        OperationType operationType,

        @JsonProperty(value = "dChangedByUserId")
        Long changedByUserId,

        @JsonProperty(value = "dChangedByUserName")
        String changedByUsername,

        @JsonProperty(value = "dtCreatedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant createdAt
) {
}
