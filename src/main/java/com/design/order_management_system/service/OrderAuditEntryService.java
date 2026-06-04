package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderAuditEntry;
import com.design.order_management_system.model.enumeration.OrderOperation;
import com.design.order_management_system.repository.OrderAuditEntryRepository;
import com.design.order_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAuditEntryService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final OrderToOrderResponse orderToOrderResponse;
    private final OrderAuditEntryRepository orderAuditEntryRepository;

    @Transactional
    public void saveOrderAuditEntry(Long userId, Order order, OrderOperation operation) {
        var orderId = order.getId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("order audit entry creation failed; userId={} orderId={} reason=user_not_found", userId, orderId);
                    return new ResourceNotFoundException(CommonConstants.USER, "id", String.valueOf(userId));
                });

        var nextVersion = orderAuditEntryRepository.getNextAuditVersionByOrderId(order.getId());
        var orderSnapshot = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(orderToOrderResponse.apply(order));

        var orderAuditEntry = OrderAuditEntry.builder()
                .version(nextVersion)
                .operation(operation)
                .snapshot(orderSnapshot)
                .user(user)
                .order(order)
                .build();

        var savedAuditEntry = orderAuditEntryRepository.save(orderAuditEntry);
        log.info("Order audit entry saved; userId={} orderId={} auditEntryId={}", userId, orderId, savedAuditEntry.getId());
    }
}
