package com.design.order_management_system.service;

import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.GeneratorUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderToOrderResponse orderToOrderResponse;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName(value = """
            If the creation of a new Draft order fails due to concurrent creation in another thread,
            The getDraftOrder method should return the concurrently created draft order after handling DataIntegrityViolationException.
            """)
    void getDraftOrder_WhenConcurrentSaveOccurs_ShouldReturnExistingOrder() {
        var userId = 1L;
        var user = User.builder()
                .id(userId).username(GeneratorUtils.generateUUID()).password("lkjsemr,xc9809w").roles(Set.of()).build();
        var orderId = 1L;
        var order = Order.builder()
                .id(orderId)
                .user(user)
                .orderStatus(OrderStatus.CREATED)
                .orderItems(List.of())
                .build();

        when(orderRepository.findOrderByUser_IdAndOrderStatus(userId, OrderStatus.CREATED))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(order));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("concurrent creation"));

        var result = orderService.getDraftOrder(userId);

        Assertions.assertThat(result).isEqualTo(order);

        verify(orderRepository, times(2)).findOrderByUser_IdAndOrderStatus(userId, OrderStatus.CREATED);
        verify(userRepository).findById(userId);

        var saveOrderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(saveOrderArgumentCaptor.capture());
        var failedOrder = saveOrderArgumentCaptor.getValue();
        Assertions.assertThat(failedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(failedOrder.getOrderItems()).isEmpty();
        Assertions.assertThat(failedOrder.getUser()).isEqualTo(user);
    }
}
