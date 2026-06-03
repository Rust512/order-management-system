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
import com.design.order_management_system.repository.OrderItemRepository;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import com.design.order_management_system.test_utils.TestSecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderItemToOrderItemResponse orderItemToOrderItemResponse;
    private OrderToOrderResponse orderToOrderResponse;
    private OrderService orderService;

    private static final long USER_ID = 2L;

    @BeforeEach
    void setUp() {
        orderToOrderResponse = spy(new OrderToOrderResponse(orderItemToOrderItemResponse));
        orderService = new OrderService(userRepository, orderRepository, orderToOrderResponse, orderItemRepository, productRepository);
    }

    @Test
    @DisplayName(value = """
            When the logged in user has ADMIN access, and the order with the given ID is found,
            The getOrderById method should return the order corresponding to the given order ID.
            """)
    void getOrderById_WhenUserIsAdminAndOrderFound_ShouldReturnTheCorrespondingOrder() {
        var orderId = 1L;
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_ADMIN);

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
        verify(orderRepository, never()).getOrderByIdAndUserIdWithItems(orderId, USER_ID);
        verify(orderToOrderResponse).apply(orderCaptor.capture());

        Assertions.assertThat(orderCaptor.getValue()).isEqualTo(order);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            When the logged in user has ADMIN access, and the order with the given ID is not found,
            The getOrderById method should throw a ResourceNotFoundException.
            """)
    void getOrderById_WhenUserIsAdminAndOrderNotFound_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_ADMIN);

        when(orderRepository.getOrderByIdWithItems(orderId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER,
                        "id",
                        orderId));

        verify(orderRepository).getOrderByIdWithItems(orderId);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            When the logged in user has USER access, and the order with the given ID owned by the user is found,
            The getOrderById method should return the order corresponding to the given order ID.
            """)
    void getOrderById_WhenUserNotAdminAndOrderFound_ShouldReturnTheCorrespondingOrder() {
        var orderId = 1L;
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .orderItems(List.of())
                .build();

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderRepository.getOrderByIdAndUserIdWithItems(orderId, USER_ID)).thenReturn(Optional.of(order));

        var orderResponse = orderService.getOrderById(orderId);
        Assertions.assertThat(orderResponse).isNotNull();
        Assertions.assertThat(orderResponse.getOrderId()).isEqualTo(orderId);
        Assertions.assertThat(orderResponse.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(orderResponse.getCreatedAt()).isNotNull();
        Assertions.assertThat(orderResponse.getTotalPrice()).isZero();
        Assertions.assertThat(orderResponse.getOrderItems()).isEmpty();

        var orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, never()).getOrderByIdWithItems(orderId);
        verify(orderRepository).getOrderByIdAndUserIdWithItems(orderId, USER_ID);
        verify(orderToOrderResponse).apply(orderCaptor.capture());

        Assertions.assertThat(orderCaptor.getValue()).isEqualTo(order);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            When the logged in user does not have ADMIN access, and the order with the given ID is not found,
            The getOrderById method should throw a ResourceNotFoundException.
            """)
    void getOrderById_WhenUserNotAdminAndOrderNotFound_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        when(orderRepository.existsById(orderId)).thenReturn(false);

        Assertions.assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER,
                        "id",
                        orderId));

        verify(orderRepository).existsById(orderId);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            When the logged in user does not have ADMIN access,
            and the order with the given ID is found, but not owned by the user,
            The getOrderById method should throw a ResourceNotOwnedException.
            """)
    void getOrderById_WhenUserNotAdminAndOrderFoundButNowOwned_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        when(orderRepository.existsById(orderId)).thenReturn(true);
        when(orderRepository.getOrderByIdAndUserIdWithItems(orderId, USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResourceNotOwnedException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_OWNED, CommonConstants.ORDER,
                        "id",
                        orderId,
                        USER_ID));

        verify(orderRepository).existsById(orderId);
        verifyNoMoreInteractions(orderRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            If the creation of a new Draft order fails due to concurrent creation in another thread,
            The getDraftOrder method should return the concurrently created draft order after handling DataIntegrityViolationException.
            """)
    void getDraftOrder_WhenConcurrentSaveOccurs_ShouldReturnExistingOrder() {
        var user = User.builder()
                .id(USER_ID).username(GeneratorUtils.generateUUID()).password("lkjsemr,xc9809w").roles(Set.of()).build();
        var orderId = 1L;
        var order = Order.builder()
                .id(orderId)
                .user(user)
                .orderStatus(OrderStatus.CREATED)
                .orderItems(List.of())
                .build();

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(order));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("concurrent creation"));

        var result = orderService.getDraftOrder(USER_ID);

        Assertions.assertThat(result).isEqualTo(order);

        verify(orderRepository, times(2)).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verify(userRepository).findById(USER_ID);

        var saveOrderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(saveOrderArgumentCaptor.capture());
        var failedOrder = saveOrderArgumentCaptor.getValue();
        Assertions.assertThat(failedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(failedOrder.getOrderItems()).isEmpty();
        Assertions.assertThat(failedOrder.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged in user ID does not exist,
            the checkoutOrder method should throw a ResourceNotFoundException
            """)
    void checkoutOrder_WhenDraftOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);
        when(orderRepository.fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.checkoutOrder())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER, "user_id", USER_ID));

        verify(orderRepository).fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository, productRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged in user ID does not have any order items in it,
            the checkoutOrder method should throw a ResourceNotFoundException
            """)
    void checkoutOrder_WhenDraftOrderIsEmpty_ShouldThrowResourceNotFoundException() {
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var orderId = 1L;
        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .build();

        when(orderRepository.fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrder_IdForRead(orderId)).thenReturn(Collections.emptyList());

        Assertions.assertThatThrownBy(() -> orderService.checkoutOrder())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER_ITEM, "order_id", orderId));

        verify(orderRepository).fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findAllByOrder_IdForRead(orderId);
        verifyNoMoreInteractions(orderRepository, orderItemRepository);
        verifyNoInteractions(productRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged in user ID does not exist,
            the cancelOrder method should throw a ResourceNotFoundException
            """)
    void cancelOrder_WhenDraftOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);
        when(orderRepository.fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.cancelOrder())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER, "user_id", USER_ID));

        verify(orderRepository).fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository, productRepository, orderToOrderResponse);
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged in user ID does exists without any items,
            the cancelOrder method should return an order response after updating the order status to CANCELLED.
            """)
    void cancelOrder_WhenDraftOrderExistsWithNoItems_ShouldReturnAfterUpdatingOrderStatus() {
        TestSecurityUtils.setAuthenticationContext(USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var orderId = 1L;
        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .build();

        when(orderRepository.fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrder_IdForRead(orderId)).thenReturn(Collections.emptyList());

        var result = orderService.cancelOrder();
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getOrderId()).isEqualTo(orderId);
        Assertions.assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).fetchDraftOrderByUserIdForUpdate(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findAllByOrder_IdForRead(orderId);

        var draftOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderToOrderResponse).apply(draftOrderCaptor.capture());

        var updatedOrder = draftOrderCaptor.getValue();
        Assertions.assertThat(updatedOrder.getId()).isEqualTo(orderId);
        Assertions.assertThat(updatedOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        Assertions.assertThat(updatedOrder.getOrderItems()).isEmpty();

        verifyNoMoreInteractions(orderRepository, orderToOrderResponse, orderItemRepository);
        verifyNoInteractions(productRepository);
        TestSecurityUtils.clearAuthenticationContext();
    }
}
