package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.OrderToOrderResponse;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.request.OrderRequest;
import com.design.order_management_system.dto.response.OrderItemResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.TestSecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
    private ProductRepository productRepository;
    @Mock
    private OrderToOrderResponse orderToOrderResponse;

    @InjectMocks
    private OrderService orderService;

    private static final Long USER_ID = 100L;

    @BeforeEach
    void setup() {
        TestSecurityUtils.authenticateUser(USER_ID, "U0", CommonConstants.ROLE_USER);
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            If the user Id in the security context is not found in the database,
            the method should throw a ResourceNotFoundException
            """)
    void registerOrder_WhenUserIdNotFound_ShouldThrowResourceNotFoundException() {
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> orderService.registerOrder(createOrderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ResourceNotFoundException.RESOURCE_NOT_FOUND,
                        CommonConstants.USER,
                        CommonConstants.ID,
                        USER_ID)
                );

        verify(userRepository).findById(USER_ID);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(productRepository, orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            If a requested product ID does not exist,
            the method should throw a ResourceNotFoundException
            """)
    void registerOrder_WhenProductIdNotFound_ShouldThrowResourceNotFoundException() {
        var user = User.builder()
                .id(USER_ID)
                .build();
        Long productId = 1L;
        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(1L)
                .build();
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest))
                .build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of());

        Assertions.assertThatThrownBy(() -> orderService.registerOrder(createOrderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(ResourceNotFoundException.RESOURCE_NOT_FOUND,
                        CommonConstants.PRODUCT,
                        CommonConstants.ID,
                        productId)
                );

        verify(userRepository).findById(USER_ID);
        verify(productRepository).findAllById(List.of(productId));
        verifyNoMoreInteractions(userRepository, productRepository);
        verifyNoInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            If the requested product has insufficient stock,
            the method should throw a InsufficientResourcesException
            """)
    void registerOrder_WhenInsufficientProductStock_ShouldThrowInsufficientResourcesException() {
        var user = User.builder()
                .id(USER_ID)
                .build();
        Long productId = 1L;
        Long stock = 1L;
        var product = Product.builder()
                .id(productId)
                .name("Pr0")
                .price(BigDecimal.valueOf(1.0))
                .stock(stock)
                .build();
        Long quantity = 2L;
        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest))
                .build();
        var productIdList = List.of(productId);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(productIdList)).thenReturn(List.of(product));

        Assertions.assertThatThrownBy(() -> orderService.registerOrder(createOrderRequest))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessage(String.format(InsufficientResourcesException.INSUFFICIENT_RESOURCES,
                        CommonConstants.PRODUCT,
                        CommonConstants.STOCK,
                        quantity,
                        stock)
                );
        Assertions.assertThat(product.getStock()).isEqualTo(stock);

        verify(userRepository).findById(USER_ID);
        verify(productRepository).findAllById(productIdList);
        verifyNoMoreInteractions(userRepository, productRepository);
        verifyNoInteractions(orderRepository, orderToOrderResponse);
    }

    @Test
    @DisplayName(value = """
            If the requested product has sufficient stock,
            the method should return an OrderResponse object
            """)
    void registerOrder_WhenSufficientProductStock_ShouldReturnOrderResponse() {
        var user = User.builder()
                .id(USER_ID)
                .build();
        Long productId = 1L;
        String productName = "Pr0";
        Long stock = 3L;
        BigDecimal price = BigDecimal.valueOf(20.0);
        var product = Product.builder()
                .id(productId)
                .name(productName)
                .price(price)
                .stock(stock)
                .build();
        Long quantity = 2L;
        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest))
                .build();
        var productIdList = List.of(productId);
        Long orderId = 1L;
        var orderItem = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .purchasePrice(price)
                .build();
        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .user(user)
                .build();
        order.addOrderItem(orderItem);
        var orderItemResponse = OrderItemResponse.builder()
                .productName(productName)
                .quantity(quantity)
                .purchasePrice(price)
                .build();
        var expectedResponse = OrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(order.getCreatedAt())
                .totalPrice(BigDecimal.valueOf(quantity).multiply(price))
                .orderItems(List.of(orderItemResponse))
                .build();


        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(productIdList)).thenReturn(List.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderToOrderResponse.apply(order)).thenReturn(expectedResponse);

        var response = orderService.registerOrder(createOrderRequest);

        Assertions.assertThat(response).isEqualTo(expectedResponse);

        verify(userRepository).findById(USER_ID);
        verify(productRepository).findAllById(productIdList);

        Assertions.assertThat(product.getStock()).isEqualTo(stock - quantity);

        ArgumentCaptor<Order> unsavedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(unsavedOrderCaptor.capture());

        var unsavedOrder = unsavedOrderCaptor.getValue();
        Assertions.assertThat(unsavedOrder.getUser()).isEqualTo(user);
        Assertions.assertThat(unsavedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(unsavedOrder.getOrderItems())
                .allSatisfy(item -> {
                    Assertions.assertThat(item.getProduct()).isEqualTo(product);
                    Assertions.assertThat(item.getQuantity()).isEqualTo(quantity);
                    Assertions.assertThat(item.getPurchasePrice()).isEqualByComparingTo(price);
                })
                .hasSize(1);
    }

    @Test
    @DisplayName(value = """
            If the requested products (this test verifies for two) have sufficient stock,
            the method should return an OrderResponse object
            """)
    void registerOrder_WhenProductsHaveSufficientStock_ShouldReturnOrderResponse() {
        var user = User.builder()
                .id(USER_ID)
                .build();
        Long productId0 = 1L;
        Long productId1 = 2L;
        String productName0 = "Pr0";
        String productName1 = "Pr1";
        Long stock0 = 3L;
        Long stock1 = 4L;
        BigDecimal price0 = BigDecimal.valueOf(20.0);
        BigDecimal price1 = BigDecimal.valueOf(30.0);
        var product0 = Product.builder()
                .id(productId0)
                .name(productName0)
                .price(price0)
                .stock(stock0)
                .build();
        var product1 = Product.builder()
                .id(productId1)
                .name(productName1)
                .price(price1)
                .stock(stock1)
                .build();
        Long quantity = 2L;
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(productId0)
                .quantity(quantity)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(productId1)
                .quantity(quantity)
                .build();
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();
        var productIdList = List.of(productId0, productId1);
        Long orderId = 1L;
        var orderItem0 = OrderItem.builder()
                .product(product0)
                .quantity(quantity)
                .purchasePrice(price0)
                .build();
        var item0Price = BigDecimal.valueOf(quantity).multiply(price0);
        var orderItem1 = OrderItem.builder()
                .product(product1)
                .quantity(quantity)
                .purchasePrice(price1)
                .build();
        var item1Price = BigDecimal.valueOf(quantity).multiply(price1);
        var totalPrice = item0Price.add(item1Price);
        var order = Order.builder()
                .id(orderId)
                .orderStatus(OrderStatus.CREATED)
                .user(user)
                .build();
        order.addOrderItem(orderItem0);
        order.addOrderItem(orderItem1);
        var orderItemResponse0 = OrderItemResponse.builder()
                .productName(productName0)
                .quantity(quantity)
                .purchasePrice(price0)
                .build();
        var orderItemResponse1 = OrderItemResponse.builder()
                .productName(productName1)
                .quantity(quantity)
                .purchasePrice(price1)
                .build();
        var expectedResponse = OrderResponse.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(order.getCreatedAt())
                .totalPrice(totalPrice)
                .orderItems(List.of(orderItemResponse0, orderItemResponse1))
                .build();


        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findAllById(productIdList)).thenReturn(List.of(product0, product1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderToOrderResponse.apply(order)).thenReturn(expectedResponse);

        var response = orderService.registerOrder(createOrderRequest);

        Assertions.assertThat(response).isEqualTo(expectedResponse);

        verify(userRepository).findById(USER_ID);
        verify(productRepository).findAllById(productIdList);

        Assertions.assertThat(product0.getStock()).isEqualTo(stock0 - quantity);
        Assertions.assertThat(product1.getStock()).isEqualTo(stock1 - quantity);

        ArgumentCaptor<Order> unsavedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(unsavedOrderCaptor.capture());

        var unsavedOrder = unsavedOrderCaptor.getValue();
        Assertions.assertThat(unsavedOrder.getUser()).isEqualTo(user);
        Assertions.assertThat(unsavedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(unsavedOrder.getOrderItems())
                .hasSize(2)
                .allSatisfy(item -> Assertions.assertThat(item.getQuantity()).isEqualTo(quantity));

        var unsavedOrderItems = unsavedOrder.getOrderItems();
        Assertions.assertThat(unsavedOrderItems.getFirst())
                .returns(product0, OrderItem::getProduct)
                .returns(price0, OrderItem::getPurchasePrice);

        Assertions.assertThat(unsavedOrderItems.get(1))
                .returns(product1, OrderItem::getProduct)
                .returns(price1, OrderItem::getPurchasePrice);
    }
}