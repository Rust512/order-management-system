package com.design.order_management_system.converter;

import com.design.order_management_system.dto.response.ProductAuditEntryResponse;
import com.design.order_management_system.model.domain.ProductAuditEntry;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class ProductAuditEntryToResponse
        implements Function<ProductAuditEntry, ProductAuditEntryResponse> {
    @Override
    public ProductAuditEntryResponse apply(ProductAuditEntry source) {
        var user = source.getUser();
        return new ProductAuditEntryResponse(
                source.getVersion(),
                source.getProductName(),
                source.getPrice(),
                source.getStock(),
                source.getOperationType(),
                user.getId(),
                user.getUsername(),
                source.getCreatedAt());
    }
}
