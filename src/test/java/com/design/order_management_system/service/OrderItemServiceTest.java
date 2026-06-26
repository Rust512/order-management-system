package com.design.order_management_system.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.repository.OrderItemRepository;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import com.design.order_management_system.test_utils.TestSecurityUtils;
import java.math.BigDecimal;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {
    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderToOrderResponse orderToOrderResponse;
    @Mock private OrderAuditEntryService orderAuditEntryService;

    @InjectMocks private OrderItemService orderItemService;

    private static final long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        TestSecurityUtils.setAuthenticationContext(
                USER_ID, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(
            value =
                    """
            If the requested product ID does not exist,
            the addOrderItem method should throw a ResourceNotFoundException
            """)
    void addOrderItem_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        var orderId = 1L;
        var productId = 2L;
        var orderItemRequest = OrderItemRequest.builder().productId(productId).quantity(1L).build();
        var order = createOrder(orderId);

        when(orderService.getDraftOrder(USER_ID)).thenReturn(order);
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderItemService.addOrderItem(orderItemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        String.format(
                                ErrorMessageConstants.RESOURCE_NOT_FOUND,
                                CommonConstants.PRODUCT,
                                "id",
                                productId));

        verify(orderService).getDraftOrder(USER_ID);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verify(productRepository).findByIdForUpdate(productId);
        verifyNoMoreInteractions(orderService, orderItemRepository, productRepository);
        verifyNoInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the product corresponding to the requested product ID has insufficient stock,
            the addOrderItem method should throw a InsufficientResourcesException
            """)
    void addOrderItem_WhenProductStockInsufficient_ShouldThrowInsufficientResourcesException() {
        var orderId = 1L;
        var productId = 2L;
        var stock = 3L;
        var reservedStock = 2L;
        var requestedQuantity = 2L;
        var product =
                Product.builder()
                        .id(productId)
                        .name("3kh234987re")
                        .price(BigDecimal.TEN)
                        .stock(stock)
                        .reservedStock(reservedStock)
                        .build();
        var orderItemRequest =
                OrderItemRequest.builder().productId(productId).quantity(requestedQuantity).build();

        var order = createOrder(orderId);

        when(orderService.getDraftOrder(USER_ID)).thenReturn(order);
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));

        Assertions.assertThatThrownBy(() -> orderItemService.addOrderItem(orderItemRequest))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessage(
                        String.format(
                                ErrorMessageConstants.INSUFFICIENT_RESOURCES,
                                CommonConstants.PRODUCT,
                                "stock - reserved_stock",
                                requestedQuantity,
                                product.getAvailableStock()));

        verify(orderService).getDraftOrder(USER_ID);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verify(productRepository).findByIdForUpdate(productId);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the draft order corresponding to the logged in user ID does not exist,
            the editOrderItem method should throw a ResourceNotFoundException
            """)
    void editOrderItem_WhenDraftOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 2L;
        var orderItemRequest = OrderItemRequest.builder().productId(productId).quantity(1L).build();

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderItemService.editOrderItem(orderItemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        String.format(
                                ErrorMessageConstants.RESOURCE_NOT_FOUND,
                                CommonConstants.ORDER,
                                "userId",
                                USER_ID));

        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(orderItemRepository, productRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the order item corresponding to the requested product ID does not exist,
            the editOrderItem method should throw a ResourceNotFoundException
            """)
    void editOrderItem_WhenOrderItemDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 2L;
        var orderItemRequest = OrderItemRequest.builder().productId(productId).quantity(1L).build();
        var orderId = 3L;
        var order = createOrder(orderId);

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.empty());

        var expectedMessage =
                String.format(
                        ErrorMessageConstants.RESOURCE_NOT_FOUND,
                        CommonConstants.ORDER_ITEM,
                        "(orderId, productId)",
                        String.format("(%s, %s)", orderId, productId));

        Assertions.assertThatThrownBy(() -> orderItemService.editOrderItem(orderItemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);

        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verifyNoMoreInteractions(orderRepository, orderItemRepository);
        verifyNoInteractions(productRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the requested product ID does not exist,
            the editOrderItem method should throw a ResourceNotFoundException
            """)
    void editOrderItem_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 2L;
        var orderItemRequest = OrderItemRequest.builder().productId(productId).quantity(1L).build();
        var orderId = 1L;
        var order = createOrder(orderId);
        var orderItemId = 3L;
        var orderItem = createOrderItem(orderItemId, 3L, BigDecimal.TWO);
        order.addOrderItem(orderItem);

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.of(orderItem));
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderItemService.editOrderItem(orderItemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        String.format(
                                ErrorMessageConstants.RESOURCE_NOT_FOUND,
                                CommonConstants.PRODUCT,
                                "id",
                                productId));

        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verify(productRepository).findByIdForUpdate(productId);
        verifyNoMoreInteractions(orderRepository, orderItemRepository, productRepository);
        verifyNoInteractions(orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the difference between the requested quantity and the reserved quantity
            corresponding to the given product ID is more than the available stock,
            the editOrderItem method should throw a InsufficientResourcesException
            """)
    void
            editOrderItem_WhenQuantityDifferenceGreaterThanAvailableStock_ShouldThrowInsufficientResourcesException() {
        var productId = 2L;
        var stock = 4L;
        var reservedStock = 3L;
        var productPrice = BigDecimal.TEN;
        var product =
                Product.builder()
                        .id(productId)
                        .name("lkweroiudsf809x8cvkj23l")
                        .price(productPrice)
                        .stock(stock)
                        .reservedStock(reservedStock)
                        .build();
        var availableStock = stock - reservedStock;
        var orderItemId = 4L;
        var quantity = 2L;
        var orderItem =
                OrderItem.builder()
                        .id(orderItemId)
                        .product(product)
                        .quantity(quantity)
                        .purchasePrice(productPrice)
                        .build();
        var orderId = 3L;
        var order = createOrder(orderId);
        order.addOrderItem(orderItem);
        var updatedQuantity = 4L;
        var orderItemRequest =
                OrderItemRequest.builder().productId(productId).quantity(updatedQuantity).build();

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.of(orderItem));
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(product));

        var expectedMessage =
                String.format(
                        ErrorMessageConstants.INSUFFICIENT_RESOURCES,
                        CommonConstants.PRODUCT,
                        "stock - reserved_stock",
                        updatedQuantity - quantity,
                        availableStock);

        Assertions.assertThatThrownBy(() -> orderItemService.editOrderItem(orderItemRequest))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessage(expectedMessage);

        verify(productRepository).findByIdForUpdate(productId);
        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verifyNoMoreInteractions(productRepository, orderRepository, orderItemRepository);
        verifyNoInteractions(orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the draft order corresponding to the logged in user ID does not exist,
            the removeOrderItem method should throw a ResourceNotFoundException
            """)
    void removeOrderItem_WhenDraftOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 2L;

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderItemService.removeOrderItem(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        String.format(
                                ErrorMessageConstants.RESOURCE_NOT_FOUND,
                                CommonConstants.ORDER,
                                "userId",
                                USER_ID));

        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(productRepository, orderItemRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the order item corresponding to the requested product ID does not exist,
            the removeOrderItem method should throw a ResourceNotFoundException
            """)
    void removeOrderItem_WhenOrderItemDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 2L;
        var orderId = 3L;
        var order = createOrder(orderId);

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.empty());

        var expectedMessage =
                String.format(
                        ErrorMessageConstants.RESOURCE_NOT_FOUND,
                        CommonConstants.ORDER_ITEM,
                        "(orderId, productId)",
                        String.format("(%s, %s)", orderId, productId));

        Assertions.assertThatThrownBy(() -> orderItemService.removeOrderItem(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(expectedMessage);

        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verifyNoMoreInteractions(orderRepository, orderItemRepository);
        verifyNoInteractions(productRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the product corresponding to the given product ID doesn't exist,
            The removeOrderItem method should throw a ResourceNotFoundException
            """)
    void removeOrderItem_WhenProductDoesNotExist_ShouldThrowResourceNotFoundException() {
        var productId = 2L;
        var orderId = 3L;
        var order = createOrder(orderId);
        var orderItemId = 4L;
        var orderItem = createOrderItem(orderItemId, 2L, BigDecimal.ONE);

        when(orderRepository.fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_IdAndProduct_IdForUpdate(orderId, productId))
                .thenReturn(Optional.of(orderItem));
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderItemService.removeOrderItem(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        String.format(
                                ErrorMessageConstants.RESOURCE_NOT_FOUND,
                                CommonConstants.PRODUCT,
                                "id",
                                productId));

        verify(orderRepository).fetchDraftOrderWithOrderItems(USER_ID, OrderStatus.CREATED);
        verify(orderItemRepository).findByOrder_IdAndProduct_IdForUpdate(orderId, productId);
        verify(productRepository).findByIdForUpdate(productId);
        verifyNoMoreInteractions(orderRepository, orderItemRepository, productRepository);
        verifyNoInteractions(orderToOrderResponse);
    }

    private Order createOrder(Long orderId) {
        return Order.builder().id(orderId).orderStatus(OrderStatus.CREATED).build();
    }

    private OrderItem createOrderItem(Long orderItemId, Long quantity, BigDecimal price) {
        return OrderItem.builder().id(orderItemId).quantity(quantity).purchasePrice(price).build();
    }
}
