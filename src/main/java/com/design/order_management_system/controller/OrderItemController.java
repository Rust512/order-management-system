package com.design.order_management_system.controller;

import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/orders/items")
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping
    ResponseEntity<OrderResponse> addOrderItem(@Valid @RequestBody OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.addOrderItem(orderItemRequest));
    }
}
