package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.converter.OrderAuditEntryToResponse;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.exception.ResourceNotOwnedException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderOperation;
import com.design.order_management_system.repository.OrderAuditEntryRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import com.design.order_management_system.test_utils.TestSecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAuditEntryServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderAuditEntryRepository orderAuditEntryRepository;
    @Mock
    private OrderItemSnapshotService orderItemSnapshotService;
    @Mock
    private OrderAuditEntryToResponse orderAuditEntryToResponse;

    @InjectMocks
    private OrderAuditEntryService orderAuditEntryService;

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName("""
            In the saveOrderAuditEntry method, if a user with the given user ID does not exist,
            a ResourceNotFoundException should be thrown.
            """)
    void saveOrderAuditEntry_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        var userId = 1L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_ADMIN);

        var order = mock(Order.class);
        var operation = OrderOperation.CANCEL;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        var expectedMessage = String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.USER, "id", userId);

        Assertions.assertThatThrownBy(() -> orderAuditEntryService.saveOrderAuditEntry(userId, order, operation))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(orderAuditEntryRepository, orderItemSnapshotService, orderAuditEntryToResponse);
    }

    @Test
    @DisplayName("""
            If the logged-in user is an admin, and the given order ID does not exist,
            the getOrderAuditEntryByVersion method should throw a ResourceNotFoundException.
            """)
    void getOrderAuditEntryByVersion_WhenAdminUserAndOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        TestSecurityUtils.setAuthenticationContext(1L, GeneratorUtils.generateUUID(), CommonConstants.ROLE_ADMIN);

        var orderId = 2L;
        var version = 1L;

        when(orderAuditEntryRepository.findByOrderIdAndVersion(orderId, version)).thenReturn(Optional.empty());

        var expectedMessage = String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER_AUDIT_ENTRY, "(orderId, version)", String.format("(%d, %d)", orderId, version));

        Assertions.assertThatThrownBy(() -> orderAuditEntryService.getOrderAuditEntryByVersion(orderId, version))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);

        verify(orderAuditEntryRepository).findByOrderIdAndVersion(orderId, version);
        verifyNoMoreInteractions(orderAuditEntryRepository);
        verifyNoInteractions(orderItemSnapshotService, orderAuditEntryToResponse, userRepository);
    }

    @Test
    @DisplayName("""
            If the logged-in user is NOT an admin, and the given order ID does not exist,
            the getOrderAuditEntryByVersion method should throw a ResourceNotFoundException.
            """)
    void getOrderAuditEntryByVersion_WhenUserNotAdminAndOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        TestSecurityUtils.setAuthenticationContext(1L, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var orderId = 2L;
        var version = 1L;

        when(orderAuditEntryRepository.existsByOrderIdAndVersion(orderId, version)).thenReturn(false);

        var expectedMessage = String.format(ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER_AUDIT_ENTRY, "(orderId, version)", String.format("(%d, %d)", orderId, version));

        Assertions.assertThatThrownBy(() -> orderAuditEntryService.getOrderAuditEntryByVersion(orderId, version))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);

        verify(orderAuditEntryRepository, never()).findByOrderIdAndVersion(orderId, version);
        verify(orderAuditEntryRepository).existsByOrderIdAndVersion(orderId, version);
        verifyNoMoreInteractions(orderAuditEntryRepository);
        verifyNoInteractions(orderItemSnapshotService, orderAuditEntryToResponse, userRepository);
    }

    @Test
    @DisplayName("""
            If the logged-in user is NOT an admin, and the given order ID exists, but is not owned by the logged-in user,
            the getOrderAuditEntryByVersion method should throw a ResourceNotOwnedException.
            """)
    void getOrderAuditEntryByVersion_WhenUserNotAdminAndOrderExistsButNotOwned_ShouldThrowResourceNotOwnedException() {
        var userId = 1L;
        TestSecurityUtils.setAuthenticationContext(userId, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var orderId = 2L;
        var version = 1L;

        when(orderAuditEntryRepository.existsByOrderIdAndVersion(orderId, version)).thenReturn(true);
        when(orderAuditEntryRepository.findByOrderIdAndUserIdAndVersion(orderId, userId, version)).thenReturn(Optional.empty());

        var expectedMessage = String.format(ErrorMessageConstants.RESOURCE_NOT_OWNED, CommonConstants.ORDER_AUDIT_ENTRY, "(orderId, version)", String.format("(%d, %d)", orderId, version), userId);

        Assertions.assertThatThrownBy(() -> orderAuditEntryService.getOrderAuditEntryByVersion(orderId, version))
                .isInstanceOf(ResourceNotOwnedException.class)
                .hasMessage(expectedMessage);

        verify(orderAuditEntryRepository, never()).findByOrderIdAndVersion(orderId, version);
        verify(orderAuditEntryRepository).existsByOrderIdAndVersion(orderId, version);
        verify(orderAuditEntryRepository).findByOrderIdAndUserIdAndVersion(orderId, userId, version);
        verifyNoMoreInteractions(orderAuditEntryRepository);
        verifyNoInteractions(orderItemSnapshotService, orderAuditEntryToResponse, userRepository);
    }
}