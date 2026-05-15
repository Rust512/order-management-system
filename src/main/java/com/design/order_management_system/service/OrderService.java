package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.request.OrderRequest;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderToOrderResponse orderToOrderResponse;

    @Transactional
    public OrderResponse registerOrder(OrderRequest orderRequest) {
        var userId = SecurityUtils.getPrincipalUser().getUserId();
        log.info("Order registration requested; userId={}", userId);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Order registration failed; userId={} reason=user_not_found", userId);
                    return new ResourceNotFoundException(
                            CommonConstants.USER,
                            "id",
                            String.valueOf(userId)
                    );
                });

        var order = Order.builder()
                .createdAt(Instant.now())
                .orderStatus(OrderStatus.CREATED)
                .user(user)
                .build();

        var productIds = orderRequest.getOrderItems()
                .stream()
                .map(OrderItemRequest::getProductId)
                .toList();

        var productIdMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        var orderItems = orderRequest.getOrderItems()
                .stream()
                .map(orderItemRequest -> {
                    Long productId = orderItemRequest.getProductId();
                    if (!productIdMap.containsKey(productId)) {
                        log.warn("Order registration failed; userId={} productId={} reason=product_not_found", userId, productId);
                        throw new ResourceNotFoundException(
                                CommonConstants.PRODUCT,
                                "id",
                                String.valueOf(productId)
                        );
                    }
                    Product product = productIdMap.get(productId);
                    product.setStock(getUpdatedStock(orderItemRequest, product, userId));

                    return OrderItem.builder()
                            .product(product)
                            .quantity(orderItemRequest.getQuantity())
                            .purchasePrice(product.getPrice())
                            .build();
                })
                .toList();

        orderItems.forEach(order::addOrderItem);

        var savedOrder = orderRepository.save(order);

        log.info("Order registered; orderId={}", savedOrder.getId());

        return orderToOrderResponse.apply(savedOrder);
    }

    private Long getUpdatedStock(OrderItemRequest orderItemRequest, Product product, Long userId) {
        Long requestedQuantity = orderItemRequest.getQuantity();
        Long availableStock = product.getStock();

        if (requestedQuantity.compareTo(availableStock) > 0) {
            log.warn(
                    "Order registration failed; userId={} productId={} requestedQuantity={} availableStock={} reason=stock_insufficient",
                    userId,
                    product.getId(),
                    requestedQuantity,
                    availableStock
            );
            throw new InsufficientResourcesException(
                    CommonConstants.PRODUCT,
                    "stock",
                    requestedQuantity,
                    availableStock
            );
        }

        return availableStock - requestedQuantity;
    }

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
