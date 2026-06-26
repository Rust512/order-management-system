package com.design.order_management_system.dto.response;

import com.design.order_management_system.model.domain.OrderSnapshot;
import com.design.order_management_system.model.enumeration.OrderOperation;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Builder;

@Builder
public record OrderAuditEntryResponse(
        @JsonProperty("dVersion") Long version,
        @JsonProperty("sOperation") OrderOperation operation,
        @JsonProperty("oSnapshot") OrderSnapshot snapshot,
        @JsonProperty("dtCreatedAt") Instant createdAt,
        @JsonProperty("dChangedByUserId") Long changedByUserId,
        @JsonProperty("sChangedByUsername") String changedByUsername) {}
