package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderAuditEntryToResponse;
import com.design.order_management_system.dto.response.OrderAuditEntryResponse;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.exception.ResourceNotOwnedException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderAuditEntry;
import com.design.order_management_system.model.domain.OrderSnapshot;
import com.design.order_management_system.model.enumeration.OrderOperation;
import com.design.order_management_system.repository.OrderAuditEntryRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAuditEntryService {
  private final UserRepository userRepository;
  private final OrderAuditEntryRepository orderAuditEntryRepository;
  private final OrderItemSnapshotService orderItemSnapshotService;
  private final OrderAuditEntryToResponse orderAuditEntryToResponse;

  @Transactional(propagation = Propagation.MANDATORY)
  public void saveOrderAuditEntry(Long userId, Order order, OrderOperation operation) {
    var orderId = order.getId();
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "order audit entry creation failed; userId={} orderId={} reason=user_not_found",
                      userId,
                      orderId);
                  return new ResourceNotFoundException(
                      CommonConstants.USER, "id", String.valueOf(userId));
                });

    var nextVersion = orderAuditEntryRepository.getNextAuditVersionByOrderId(orderId);

    var itemSnapshots = order.getOrderItems().stream().map(orderItemSnapshotService).toList();

    var totalPrice =
        itemSnapshots.stream()
            .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(item.getPurchasePrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    var orderSnapshot =
        OrderSnapshot.builder()
            .orderId(orderId)
            .orderStatus(order.getOrderStatus())
            .totalPrice(totalPrice)
            .orderItemSnapshots(itemSnapshots)
            .build();

    var orderAuditEntry =
        OrderAuditEntry.builder()
            .version(nextVersion)
            .operation(operation)
            .snapshot(orderSnapshot)
            .user(user)
            .order(order)
            .build();

    var savedAuditEntry = orderAuditEntryRepository.save(orderAuditEntry);
    log.info(
        "Order audit entry saved; userId={} orderId={} auditEntryId={}",
        userId,
        orderId,
        savedAuditEntry.getId());
  }

  @Transactional(readOnly = true)
  public PagedResponse<OrderAuditEntryResponse> getOrderAuditEntries(
      Long orderId, Pageable pageable) {
    var principalUser = SecurityUtils.getPrincipalUser();
    var userId = principalUser.getUserId();

    Page<OrderAuditEntry> pages =
        SecurityUtils.isAdmin(principalUser)
            ? orderAuditEntryRepository.findByOrderId(orderId, pageable)
            : orderAuditEntryRepository.findByOrderIdAndUserId(orderId, userId, pageable);

    var content = pages.getContent().stream().map(orderAuditEntryToResponse).toList();

    return PagedResponse.<OrderAuditEntryResponse>builder()
        .content(content)
        .page(pages.getNumber())
        .size(pages.getSize())
        .totalElements(pages.getTotalElements())
        .totalPages(pages.getTotalPages())
        .build();
  }

  @Transactional(readOnly = true)
  public OrderAuditEntryResponse getOrderAuditEntryByVersion(Long orderId, Long version) {
    var principalUser = SecurityUtils.getPrincipalUser();
    var userId = principalUser.getUserId();

    if (SecurityUtils.isAdmin(principalUser)) {
      log.debug(
          "Get order audit entry with admin access; userId={} orderId={} version={}",
          userId,
          orderId,
          version);
      return orderAuditEntryRepository
          .findByOrderIdAndVersion(orderId, version)
          .map(orderAuditEntryToResponse)
          .orElseThrow(
              () -> {
                log.warn(
                    "Get order audit entry failed; userId={} orderId={} version={} reason=audit_entry_not_found",
                    userId,
                    orderId,
                    version);
                return new ResourceNotFoundException(
                    CommonConstants.ORDER_AUDIT_ENTRY,
                    "(orderId, version)",
                    String.format("(%d, %d)", orderId, version));
              });
    }

    log.debug(
        "Get order audit entry with ownership access; userId={} orderId={} version={}",
        userId,
        orderId,
        version);

    if (!orderAuditEntryRepository.existsByOrderIdAndVersion(orderId, version)) {
      log.warn(
          "Get order audit entry failed; userId={} orderId={} version={} reason=audit_entry_not_found",
          userId,
          orderId,
          version);
      throw new ResourceNotFoundException(
          CommonConstants.ORDER_AUDIT_ENTRY,
          "(orderId, version)",
          String.format("(%d, %d)", orderId, version));
    }

    return orderAuditEntryRepository
        .findByOrderIdAndUserIdAndVersion(orderId, userId, version)
        .map(orderAuditEntryToResponse)
        .orElseThrow(
            () -> {
              log.warn(
                  "Get order audit entry failed; userId={} orderId={} version={} reason=order_not_owned",
                  userId,
                  orderId,
                  version);
              return new ResourceNotOwnedException(
                  CommonConstants.ORDER_AUDIT_ENTRY,
                  "(orderId, version)",
                  String.format("(%d, %d)", orderId, version),
                  userId);
            });
  }
}
