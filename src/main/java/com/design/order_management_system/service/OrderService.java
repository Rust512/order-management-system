package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
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

    @Transactional
    public Order getDraftOrder(Long userId) {
        var optionalOrder = orderRepository.findOrderByUser_IdAndOrderStatus(userId, OrderStatus.CREATED);
        if (optionalOrder.isPresent()) {
            var order = optionalOrder.get();
            log.info("Draft order resolved; userId={} orderId={}", userId, order.getId());
            return order;
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Get draft order failed; userId={} reason=user_not_found", userId);
                    return new ResourceNotFoundException(CommonConstants.USER, "id", String.valueOf(userId));
                });
        var order = Order.builder()
                .orderStatus(OrderStatus.CREATED)
                .user(user)
                .build();

        try {
            var savedOrder = orderRepository.save(order);
            var orderId = savedOrder.getId();

            log.info("Draft order retrieved; userId={} orderId={}", userId, orderId);
            return savedOrder;
        } catch (DataIntegrityViolationException _) {
            log.info("Draft order creation retried; userId={} reason=concurrent_creation", userId);
            return orderRepository.findOrderByUser_IdAndOrderStatus(userId, OrderStatus.CREATED)
                    .orElseThrow(() -> new ResourceNotFoundException(CommonConstants.ORDER, "user", String.valueOf(userId)));
        }
    }
}
