package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PagedResponse<T> {
    @JsonProperty(value = "aContent")
    private List<T> content;

    @JsonProperty(value = "dPage")
    private int page;

    @JsonProperty(value = "dSize")
    private int size;

    @JsonProperty(value = "dTotalElements")
    private long totalElements;

    @JsonProperty(value = "dTotalPages")
    private long totalPages;
}
