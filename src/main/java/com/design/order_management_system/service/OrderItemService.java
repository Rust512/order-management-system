package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.repository.OrderItemRepository;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderToOrderResponse orderToOrderResponse;

    @Transactional
    public OrderResponse addOrderItem(OrderItemRequest orderItemRequest) {
        var user = SecurityUtils.getPrincipalUser();
        var userId = user.getUserId();
        log.debug("Add order item attempted; userId={}", userId);

        var productId = orderItemRequest.getProductId();
        var quantity = orderItemRequest.getQuantity();
        var product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> {
                    log.warn("Add order item failed; userId={} productId={} reason=product_not_found", userId, productId);
                    return new ResourceNotFoundException(
                            CommonConstants.PRODUCT,
                            "id",
                            String.valueOf(productId)
                    );
                });
        validateStockAvailability(userId, product, quantity);

        var draftOrder = orderService.getDraftOrder(userId);
        var orderId = draftOrder.getId();

        var optionalOrderItem = orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId);

        product.setReservedStock(product.getReservedStock() + quantity);

        OrderItem orderItem;
        if (optionalOrderItem.isPresent()) {
            orderItem = optionalOrderItem.get();
            orderItem.setQuantity(orderItem.getQuantity() + quantity);
            orderItem.setPurchasePrice(product.getPrice());
        } else {
            log.info("Creating new order item; userId={} productId={} orderId={}", userId, productId, orderId);
            orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .purchasePrice(product.getPrice())
                    .build();
            draftOrder.addOrderItem(orderItem);
        }

        var savedOrder = orderRepository.save(draftOrder);

        log.info("Order item added; userId={} orderId={} productId={} quantity={}", userId, orderId, productId, quantity);

        return orderToOrderResponse.apply(savedOrder);
    }

    private void validateStockAvailability(Long userId, Product product, Long requestedQuantity) {
        var availableStock = product.getAvailableStock();

        if (requestedQuantity > availableStock) {
            log.warn("Stock validation failed; userId={} productId={} reason=insufficient_stock", userId, product.getId());
            throw new InsufficientResourcesException(CommonConstants.PRODUCT, "stock - reserved_stock", requestedQuantity, availableStock);
        }
    }
}
