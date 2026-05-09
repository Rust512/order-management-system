package com.design.order_management_system.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Builder
@Validated
@AllArgsConstructor
public class PagedResponse<T> {

    @NotEmpty
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
