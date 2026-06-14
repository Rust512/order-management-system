package com.design.order_management_system.converter;

import com.design.order_management_system.dto.response.OrderAuditEntryResponse;
import com.design.order_management_system.model.domain.OrderAuditEntry;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class OrderAuditEntryToResponse implements Function<OrderAuditEntry, OrderAuditEntryResponse> {
    @Override
    public OrderAuditEntryResponse apply(OrderAuditEntry orderAuditEntry) {
        var user = orderAuditEntry.getUser();

        return OrderAuditEntryResponse.builder()
                .version(orderAuditEntry.getVersion())
                .operation(orderAuditEntry.getOperation())
                .snapshot(orderAuditEntry.getSnapshot())
                .createdAt(orderAuditEntry.getCreatedAt())
                .changedByUserId(user.getId())
                .changedByUsername(user.getUsername())
                .build();
    }
}
