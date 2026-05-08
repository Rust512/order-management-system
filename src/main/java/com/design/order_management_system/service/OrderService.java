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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderToOrderResponse orderToOrderResponse;

    @Transactional
    public OrderResponse registerOrder(OrderRequest orderRequest) {
        var userId = orderRequest.getUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        CommonConstants.USER,
                        CommonConstants.ID,
                        String.valueOf(userId)
                ));

        var order = orderRepository.save(Order.builder()
                .createdAt(Instant.now())
                .orderStatus(OrderStatus.CREATED)
                .user(user)
                .build());

        var orderItems = orderRequest.getOrderItems()
                .stream()
                .map(orderItemRequest -> {
                    Long productId = orderItemRequest.getProductId();
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    CommonConstants.PRODUCT,
                                    CommonConstants.ID,
                                    String.valueOf(productId)
                            ));
                    product.setStock(getUpdatedStock(orderItemRequest, product));

                    return OrderItem.builder()
                            .product(product)
                            .order(order)
                            .quantity(orderItemRequest.getQuantity())
                            .purchasePrice(product.getPrice())
                            .build();
                })
                .toList();

        order.setOrderItems(orderItems);

        return orderToOrderResponse.apply(order);
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
}
