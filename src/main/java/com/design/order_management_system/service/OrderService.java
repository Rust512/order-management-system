package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderToOrderResponse orderToOrderResponse;

    public OrderResponse getOrderById(Long id) {
        var principalUser = SecurityUtils.getPrincipalUser();
        Long userId = principalUser.getUserId();

        if (SecurityUtils.isAdmin(principalUser)) {
            log.debug("Fetch order using admin access; userId={} orderId={}", userId, id);
            return orderToOrderResponse.apply(orderRepository.getOrderByIdWithItems(id)
                    .orElseThrow(() -> {
                        log.warn("Fetch order failed; userId={} orderId={} reason=order_not_found", userId, id);
                        return new ResourceNotFoundException(
                                CommonConstants.ORDER,
                                "id",
                                String.valueOf(id)
                        );
                    })
            );
        }

        log.debug("Fetch order using ownership access; userId={} orderId={}", userId, id);

        var fetchedOrder = orderRepository.getOrderByIdAndUserIdWithItems(id, userId)
                .orElseThrow(() -> {
                    log.warn("Fetch order failed; userId={} orderId={} reason=order_not_accessible", userId, id);
                    return new ResourceNotFoundException(
                            CommonConstants.ORDER,
                            "id",
                            String.valueOf(id)
                    );
                });

        log.info("Order fetched; userId={} orderId={}", userId, id);

        return orderToOrderResponse.apply(fetchedOrder);
    }
}
