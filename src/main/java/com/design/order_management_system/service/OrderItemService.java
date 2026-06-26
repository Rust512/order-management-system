package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderOperation;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.repository.OrderItemRepository;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.utils.SecurityUtils;
import java.util.Objects;
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
  private final OrderAuditEntryService orderAuditEntryService;

  @Transactional
  public OrderResponse addOrderItem(OrderItemRequest orderItemRequest) {
    var user = SecurityUtils.getPrincipalUser();
    var userId = user.getUserId();
    log.debug("Add order item attempted; userId={}", userId);

    var draftOrder = orderService.getDraftOrder(userId);
    var orderId = draftOrder.getId();

    var productId = orderItemRequest.getProductId();
    var quantity = orderItemRequest.getQuantity();

    var lockedOrderItem =
        orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId);

    var product =
        productRepository
            .findByIdForUpdate(productId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Add order item failed; userId={} productId={} reason=product_not_found",
                      userId,
                      productId);
                  return new ResourceNotFoundException(
                      CommonConstants.PRODUCT, "id", String.valueOf(productId));
                });
    validateStockAvailability(userId, product, quantity);

    product.setReservedStock(product.getReservedStock() + quantity);

    OrderItem orderItem;
    OrderOperation operation;
    if (lockedOrderItem.isPresent()) {
      var orderItemId = lockedOrderItem.get().getId();
      orderItem =
          draftOrder.getOrderItems().stream()
              .filter(item -> Objects.equals(orderItemId, item.getId()))
              .findFirst()
              .orElseThrow(
                  () -> {
                    log.error(
                        "Add order item failed; userId={} orderId={} productId={} reason=order_item_not_found",
                        userId,
                        orderId,
                        productId);
                    return new IllegalStateException(
                        String.format(
                            "Order item with ID %d in order with ID %d is unexpectedly missing",
                            orderItemId, orderId));
                  });
      orderItem.setQuantity(orderItem.getQuantity() + quantity);
      orderItem.setPurchasePrice(product.getPrice());
      operation = OrderOperation.EDIT_ITEM;
    } else {
      log.info(
          "Creating new order item; userId={} productId={} orderId={}", userId, productId, orderId);
      orderItem =
          OrderItem.builder()
              .product(product)
              .quantity(quantity)
              .purchasePrice(product.getPrice())
              .build();
      draftOrder.addOrderItem(orderItem);
      operation = OrderOperation.ADD_ITEM;
    }

    var savedOrder = orderRepository.save(draftOrder);

    orderAuditEntryService.saveOrderAuditEntry(userId, savedOrder, operation);

    log.info(
        "Order item added; userId={} orderId={} productId={} quantity={}",
        userId,
        orderId,
        productId,
        quantity);

    return orderToOrderResponse.apply(savedOrder);
  }

  @Transactional
  public OrderResponse editOrderItem(OrderItemRequest orderItemRequest) {
    var user = SecurityUtils.getPrincipalUser();
    var userId = user.getUserId();
    log.debug("Edit order item attempted; userId={}", userId);

    var draftOrder =
        orderRepository
            .fetchDraftOrderWithOrderItems(userId, OrderStatus.CREATED)
            .orElseThrow(
                () -> {
                  log.warn("Edit order item failed; userId={} reason=order_not_found", userId);
                  return new ResourceNotFoundException(
                      CommonConstants.ORDER, "userId", String.valueOf(userId));
                });
    var orderId = draftOrder.getId();

    var productId = orderItemRequest.getProductId();

    var lockedOrderItem =
        orderItemRepository
            .findByOrder_IdAndProduct_IdForUpdate(orderId, productId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Edit order item failed; userId={} orderId={} productId={} reason=order_item_not_found",
                      userId,
                      orderId,
                      productId);
                  return new ResourceNotFoundException(
                      CommonConstants.ORDER_ITEM,
                      "(orderId, productId)",
                      String.format("(%s, %s)", orderId, productId));
                });

    var orderItemId = lockedOrderItem.getId();
    var orderItem =
        draftOrder.getOrderItems().stream()
            .filter(item -> Objects.equals(item.getId(), orderItemId))
            .findFirst()
            .orElseThrow(
                () -> {
                  log.error(
                      "Edit order item failed; userId={} orderId={} orderItemId={} productId={} reason=order_item_unexpectedly_missing",
                      userId,
                      orderId,
                      orderItemId,
                      productId);
                  return new IllegalStateException(
                      String.format("Order item with ID %s unexpectedly missing", orderItemId));
                });

    var product =
        productRepository
            .findByIdForUpdate(productId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Edit order item failed; userId={} productId={} reason=product_not_found",
                      userId,
                      productId);
                  return new ResourceNotFoundException(
                      CommonConstants.PRODUCT, "id", String.valueOf(productId));
                });

    var quantityDelta = orderItemRequest.getQuantity() - orderItem.getQuantity();
    if (quantityDelta > 0) {
      validateStockAvailability(userId, product, quantityDelta);
    }

    product.setReservedStock(product.getReservedStock() + quantityDelta);

    orderItem.setQuantity(orderItemRequest.getQuantity());
    orderItem.setPurchasePrice(product.getPrice());

    var savedOrder = orderRepository.save(draftOrder);

    orderAuditEntryService.saveOrderAuditEntry(userId, savedOrder, OrderOperation.EDIT_ITEM);

    log.info(
        "Order item edited; userId={} orderId={} orderItemId={} productId={} quantity={}",
        userId,
        orderId,
        orderItemId,
        productId,
        orderItemRequest.getQuantity());

    return orderToOrderResponse.apply(savedOrder);
  }

  @Transactional
  public OrderResponse removeOrderItem(Long productId) {
    var user = SecurityUtils.getPrincipalUser();
    var userId = user.getUserId();
    log.debug("Remove order item attempted; userId={}", userId);

    var draftOrder =
        orderRepository
            .fetchDraftOrderWithOrderItems(userId, OrderStatus.CREATED)
            .orElseThrow(
                () -> {
                  log.warn("Delete order item failed; userId={} reason=order_not_found", userId);
                  return new ResourceNotFoundException(
                      CommonConstants.ORDER, "userId", String.valueOf(userId));
                });
    var orderId = draftOrder.getId();

    var orderItem =
        orderItemRepository
            .findByOrder_IdAndProduct_IdForUpdate(orderId, productId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Delete order item failed; userId={} orderId={} productId={} reason=order_item_not_found",
                      userId,
                      orderId,
                      productId);
                  return new ResourceNotFoundException(
                      CommonConstants.ORDER_ITEM,
                      "(orderId, productId)",
                      String.format("(%s, %s)", orderId, productId));
                });
    var orderItemId = orderItem.getId();

    var product =
        productRepository
            .findByIdForUpdate(productId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Delete order item failed; userId={} productId={} reason=product_not_found",
                      userId,
                      productId);
                  return new ResourceNotFoundException(
                      CommonConstants.PRODUCT, "id", String.valueOf(productId));
                });

    product.setReservedStock(product.getReservedStock() - orderItem.getQuantity());

    draftOrder.getOrderItems().removeIf(item -> Objects.equals(item.getId(), orderItemId));

    var savedOrder = orderRepository.save(draftOrder);

    orderAuditEntryService.saveOrderAuditEntry(userId, savedOrder, OrderOperation.REMOVE_ITEM);

    log.info(
        "Order item deleted; userId={} productId={} orderId={} orderItemId={}",
        userId,
        productId,
        orderId,
        orderItemId);

    return orderToOrderResponse.apply(savedOrder);
  }

  private void validateStockAvailability(Long userId, Product product, Long requestedQuantity) {
    var availableStock = product.getAvailableStock();
    if (requestedQuantity > availableStock) {
      log.warn(
          "Stock validation failed; userId={} productId={} reason=insufficient_stock",
          userId,
          product.getId());
      throw new InsufficientResourcesException(
          CommonConstants.PRODUCT, "stock - reserved_stock", requestedQuantity, availableStock);
    }
  }
}
