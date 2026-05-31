package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.converter.OrderItemToOrderItemResponse;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.exception.ResourceNotOwnedException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.GeneratorUtils;
import com.design.order_management_system.utils.TestSecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemToOrderItemResponse orderItemToOrderItemResponse;
    private OrderToOrderResponse orderToOrderResponse;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderToOrderResponse = spy(new OrderToOrderResponse(orderItemToOrderItemResponse));
        orderService = new OrderService(userRepository, orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            When the logged in user has ADMIN access, and the order with the given ID is found,
            The getOrderById method should return the order corresponding to the given order ID.
            """)
    void getOrderById_WhenUserIsAdminAndOrderFound_ShouldReturnTheCorrespondingOrder() {
        var orderId = 1L;
        var userId = 2L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_ADMIN);

        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .orderItems(List.of())
                .build();

        when(orderRepository.getOrderByIdWithItems(orderId)).thenReturn(Optional.of(order));

        var orderResponse = orderService.getOrderById(orderId);
        Assertions.assertThat(orderResponse).isNotNull();
        Assertions.assertThat(orderResponse.getOrderId()).isEqualTo(orderId);
        Assertions.assertThat(orderResponse.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(orderResponse.getCreatedAt()).isNotNull();
        Assertions.assertThat(orderResponse.getTotalPrice()).isZero();
        Assertions.assertThat(orderResponse.getOrderItems()).isEmpty();

        var orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).getOrderByIdWithItems(orderId);
        verify(orderRepository, never()).getOrderByIdAndUserIdWithItems(orderId, userId);
        verify(orderToOrderResponse).apply(orderCaptor.capture());

        Assertions.assertThat(orderCaptor.getValue()).isEqualTo(order);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            When the logged in user has ADMIN access, and the order with the given ID is not found,
            The getOrderById method should throw a ResourceNotFoundException.
            """)
    void getOrderById_WhenUserIsAdminAndOrderNotFound_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        var userId = 2L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_ADMIN);

        when(orderRepository.getOrderByIdWithItems(orderId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER,
                        "id",
                        orderId));

        verify(orderRepository).getOrderByIdWithItems(orderId);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            When the logged in user has USER access, and the order with the given ID owned by the user is found,
            The getOrderById method should return the order corresponding to the given order ID.
            """)
    void getOrderById_WhenUserNotAdminAndOrderFound_ShouldReturnTheCorrespondingOrder() {
        var orderId = 1L;
        var userId = 2L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .orderItems(List.of())
                .build();

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderRepository.getOrderByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.of(order));

        var orderResponse = orderService.getOrderById(orderId);
        Assertions.assertThat(orderResponse).isNotNull();
        Assertions.assertThat(orderResponse.getOrderId()).isEqualTo(orderId);
        Assertions.assertThat(orderResponse.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(orderResponse.getCreatedAt()).isNotNull();
        Assertions.assertThat(orderResponse.getTotalPrice()).isZero();
        Assertions.assertThat(orderResponse.getOrderItems()).isEmpty();

        var orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, never()).getOrderByIdWithItems(orderId);
        verify(orderRepository).getOrderByIdAndUserIdWithItems(orderId, userId);
        verify(orderToOrderResponse).apply(orderCaptor.capture());

        Assertions.assertThat(orderCaptor.getValue()).isEqualTo(order);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            When the logged in user does not have ADMIN access, and the order with the given ID is not found,
            The getOrderById method should throw a ResourceNotFoundException.
            """)
    void getOrderById_WhenUserNotAdminAndOrderNotFound_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        var userId = 2L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        when(orderRepository.existsById(orderId)).thenReturn(false);

        Assertions.assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER,
                        "id",
                        orderId));

        verify(orderRepository).existsById(orderId);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            When the logged in user does not have ADMIN access,
            and the order with the given ID is found, but not owned by the user,
            The getOrderById method should throw a ResourceNotOwnedException.
            """)
    void getOrderById_WhenUserNotAdminAndOrderFoundButNowOwned_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        var userId = 2L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderRepository.getOrderByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotOwnedException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_OWNED, CommonConstants.ORDER,
                        "id",
                        orderId,
                        userId));

        verify(orderRepository).existsById(orderId);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
    }

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

        when(orderRepository.fetchDraftOrderWithOrderItems(userId, OrderStatus.CREATED))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(order));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("concurrent creation"));

        var result = orderService.getDraftOrder(userId);

        Assertions.assertThat(result).isEqualTo(order);

        verify(orderRepository, times(2)).fetchDraftOrderWithOrderItems(userId, OrderStatus.CREATED);
        verify(userRepository).findById(userId);

        var saveOrderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(saveOrderArgumentCaptor.capture());
        var failedOrder = saveOrderArgumentCaptor.getValue();
        Assertions.assertThat(failedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(failedOrder.getOrderItems()).isEmpty();
        Assertions.assertThat(failedOrder.getUser()).isEqualTo(user);
    }
}
