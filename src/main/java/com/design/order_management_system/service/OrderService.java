package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.exception.ResourceNotOwnedException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.repository.OrderItemRepository;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderToOrderResponse orderToOrderResponse;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

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

        if (!orderRepository.existsById(id)) {
            log.warn("Fetch order failed; userId={} orderId={} reason=order_not_found", userId, id);
            throw new ResourceNotFoundException(
                    CommonConstants.ORDER,
                    "id",
                    String.valueOf(id)
            );
        }

        log.debug("Fetch order using ownership access; userId={} orderId={}", userId, id);

        var fetchedOrder = orderRepository.getOrderByIdAndUserIdWithItems(id, userId)
                .orElseThrow(() -> {
                    log.warn("Fetch order failed; userId={} orderId={} reason=order_not_accessible", userId, id);
                    return new ResourceNotOwnedException(
                            CommonConstants.ORDER,
                            "id",
                            String.valueOf(id),
                            userId
                    );
                });

        log.info("Order fetched; userId={} orderId={}", userId, id);

        return orderToOrderResponse.apply(fetchedOrder);
    }

    @Transactional
    public Order getDraftOrder(Long userId) {
        var optionalOrder = orderRepository.fetchDraftOrderWithOrderItems(userId, OrderStatus.CREATED);
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
        } catch (DataIntegrityViolationException ex) {
            log.info("Draft order creation retried; userId={} reason=concurrent_creation", userId, ex);
            return orderRepository.fetchDraftOrderWithOrderItems(userId, OrderStatus.CREATED)
                    .orElseThrow(() -> new ResourceNotFoundException(CommonConstants.ORDER, "user", String.valueOf(userId)));
        }
    }

    @Transactional
    public OrderResponse checkoutOrder() {
        var userId = SecurityUtils.getPrincipalUser()
                .getUserId();

        log.debug("Order checkout attempted; userId={}", userId);

        var draftOrder = orderRepository.fetchDraftOrderByUserIdForUpdate(userId, OrderStatus.CREATED)
                .orElseThrow(() -> {
                    log.warn("Order checkout failed; userId={} reason=order_not_found", userId);
                    return new ResourceNotFoundException(CommonConstants.ORDER, "user_id", String.valueOf(userId));
                });
        var orderId = draftOrder.getId();

        var orderItems = orderItemRepository.findAllByOrder_IdForRead(orderId);

        if (orderItems.isEmpty()) {
            log.warn("Order checkout failed; userId={} reason=empty_order", userId);
            throw new ResourceNotFoundException(CommonConstants.ORDER_ITEM, "order_id", String.valueOf(orderId));
        }

        var productIdToOrderItemMap = orderItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), Function.identity()));

        var productIds = productIdToOrderItemMap.keySet()
                .stream()
                .sorted()
                .toList();

        productRepository.findAllByIdInForWrite(productIds)
                .forEach(product -> {
                    var item = productIdToOrderItemMap.get(product.getId());
                    var quantity = item.getQuantity();
                    product.setReservedStock(product.getReservedStock() - quantity);
                    product.setStock(product.getStock() - quantity);
                });

        draftOrder.setOrderStatus(OrderStatus.CONFIRMED);

        log.info("Order checkout success; userId={} orderId={}", userId, orderId);

        return orderToOrderResponse.apply(draftOrder);
    }
}
