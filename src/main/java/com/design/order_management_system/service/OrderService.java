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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CommonConstants.USER,
                        CommonConstants.ID,
                        String.valueOf(userId)
                ));

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
                        throw new ResourceNotFoundException(
                                CommonConstants.PRODUCT,
                                CommonConstants.ID,
                                String.valueOf(productId)
                        );
                    }
                    Product product = productIdMap.get(productId);
                    product.setStock(getUpdatedStock(orderItemRequest, product));

                    return OrderItem.builder()
                            .product(product)
                            .quantity(orderItemRequest.getQuantity())
                            .purchasePrice(product.getPrice())
                            .build();
                })
                .toList();

        orderItems.forEach(order::addOrderItem);

        return orderToOrderResponse.apply(orderRepository.save(order));
    }

    private Long getUpdatedStock(OrderItemRequest orderItemRequest, Product product) {
        Long minimumRequiredStock = orderItemRequest.getQuantity();
        Long availableStock = product.getStock();

        if (minimumRequiredStock.compareTo(availableStock) > 0) {
            throw new InsufficientResourcesException(
                    CommonConstants.PRODUCT,
                    CommonConstants.STOCK,
                    minimumRequiredStock,
                    availableStock
            );
        }

        return availableStock - minimumRequiredStock;
    }

    public OrderResponse getOrderById(Long id) {
        var principalUser = SecurityUtils.getPrincipalUser();

        if (SecurityUtils.isAdmin(principalUser)) {
            return orderToOrderResponse.apply(orderRepository.getOrderByIdWithItems(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            CommonConstants.ORDER,
                            CommonConstants.ID,
                            String.valueOf(id)
                    ))
            );
        }

        return orderToOrderResponse.apply(orderRepository.getOrderByIdAndUserIdWithItems(id, principalUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        CommonConstants.ORDER,
                        CommonConstants.ID,
                        String.valueOf(id)
                ))
        );
    }
}
