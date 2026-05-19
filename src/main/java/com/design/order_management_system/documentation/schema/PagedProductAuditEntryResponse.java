package com.design.order_management_system.documentation.schema;

import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductAuditEntryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "pagedProductAuditEntryResponse")
public class PagedProductAuditEntryResponse extends PagedResponse<ProductAuditEntryResponse> {
    public PagedProductAuditEntryResponse(List<ProductAuditEntryResponse> content, int page, int size, long totalElements, long totalPages) {
        super(content, page, size, totalElements, totalPages);
    }
}
