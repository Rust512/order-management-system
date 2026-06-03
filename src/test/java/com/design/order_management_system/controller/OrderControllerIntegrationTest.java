package com.design.order_management_system.controller;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerIntegrationTest extends DatabaseTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    private static final String NORMAL_PASSWORD = "NRL@5896";
    private static final String NORMAL_USERNAME = GeneratorUtils.generateUUID();

    @BeforeAll
    void beforeAll() {
        var normalRole = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(ErrorMessageConstants.ROLE_USER_WAS_NOT_SEEDED));
        var normalUser = User.builder()
                .username(NORMAL_USERNAME)
                .password(passwordEncoder.encode(NORMAL_PASSWORD))
                .build();
        normalUser.addRole(normalRole);

        userRepository.save(normalUser);
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged-in user exists with one order item,
            the POST /v1/orders/checkout API should return an OrderResponse with order status CONFIRMED;
            the product stock and product reserved stock corresponding to the order items in the order should be updated.
            """)
    void checkoutOrder_WhenDraftOrderExists_ThenShouldChangeOrderStatusAndUpdateProductStock() {
        var owner = userRepository.findByUsername(NORMAL_USERNAME).orElseThrow();
        var productName = GeneratorUtils.generateUUID();
        var productPrice = BigDecimal.TWO;
        var stock = 5L;
        var reservedStock = 3L;
        var savedProduct = transactionTemplate.execute(_ -> {
            var product = Product.builder()
                    .name(productName)
                    .price(productPrice)
                    .stock(stock)
                    .reservedStock(reservedStock)
                    .build();
            return productRepository.save(product);
        });

        var quantity = 2L;
        var savedOrder = transactionTemplate.execute(_ -> {
            var orderItem = OrderItem.builder()
                    .product(savedProduct)
                    .quantity(quantity)
                    .purchasePrice(productPrice)
                    .build();
            var order = Order.builder()
                    .orderStatus(OrderStatus.CREATED)
                    .user(owner)
                    .build();
            order.addOrderItem(orderItem);
            return orderRepository.save(order);
        });

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);

        var entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponse> response = this.restTemplate.postForEntity("/v1/orders/checkout", entity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isEqualTo(savedOrder.getId());
        Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        Assertions.assertThat(body.getCreatedAt()).isNotNull();
        var expectedTotalPrice = BigDecimal.valueOf(quantity).multiply(productPrice);
        Assertions.assertThat(body.getTotalPrice()).isEqualByComparingTo(expectedTotalPrice);
        Assertions.assertThat(body.getOrderItems())
                .singleElement()
                .satisfies(orderItem -> {
                    Assertions.assertThat(orderItem.getProductName()).isEqualTo(productName);
                    Assertions.assertThat(orderItem.getQuantity()).isEqualTo(quantity);
                    Assertions.assertThat(orderItem.getPurchasePrice()).isEqualByComparingTo(productPrice);
                });

        transactionTemplate.executeWithoutResult(_ -> {
            var persistedProduct = productRepository.findById(savedProduct.getId())
                    .orElseThrow();
            Assertions.assertThat(persistedProduct.getStock()).isEqualTo(stock - quantity);
            Assertions.assertThat(persistedProduct.getReservedStock()).isEqualTo(reservedStock - quantity);
            var persistedOrder = orderRepository.findById(savedOrder.getId())
                    .orElseThrow();

            Assertions.assertThat(persistedOrder.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            Assertions.assertThat(persistedOrder.getOrderItems()).hasSize(1);
        });
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged-in user exists with NO order item,
            the DELETE /v1/orders API should return an OrderResponse with order status CANCELLED.
            """)
    void cancelOrder_WhenDraftOrderHasNoItems_ThenShouldChangeOrderStatusAndUpdateProductStock() {
        var owner = userRepository.findByUsername(NORMAL_USERNAME).orElseThrow();

        var savedOrder = transactionTemplate.execute(_ -> {
            var order = Order.builder()
                    .orderStatus(OrderStatus.CREATED)
                    .user(owner)
                    .build();
            return orderRepository.save(order);
        });

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);

        var entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponse> response = this.restTemplate.exchange("/v1/orders", HttpMethod.DELETE, entity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isEqualTo(savedOrder.getId());
        Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        Assertions.assertThat(body.getCreatedAt()).isNotNull();
        Assertions.assertThat(body.getTotalPrice()).isZero();
        Assertions.assertThat(body.getOrderItems()).isEmpty();

        transactionTemplate.executeWithoutResult(_ -> {
            var persistedOrder = orderRepository.findById(savedOrder.getId())
                    .orElseThrow();

            Assertions.assertThat(persistedOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    @Test
    @DisplayName(value = """
            If the draft order corresponding to the logged-in user exists with one order item,
            the DELETE /v1/orders API should return an OrderResponse with order status CANCELLED;
            the product stock corresponding to the order items in the order should be updated.
            """)
    void cancelOrder_WhenDraftOrderExists_ThenShouldChangeOrderStatusAndUpdateProductStock() {
        var owner = userRepository.findByUsername(NORMAL_USERNAME).orElseThrow();
        var productName = GeneratorUtils.generateUUID();
        var productPrice = BigDecimal.TWO;
        var stock = 5L;
        var reservedStock = 3L;
        var savedProduct = transactionTemplate.execute(_ -> {
            var product = Product.builder()
                    .name(productName)
                    .price(productPrice)
                    .stock(stock)
                    .reservedStock(reservedStock)
                    .build();
            return productRepository.save(product);
        });

        var quantity = 2L;
        var savedOrder = transactionTemplate.execute(_ -> {
            var orderItem = OrderItem.builder()
                    .product(savedProduct)
                    .quantity(quantity)
                    .purchasePrice(productPrice)
                    .build();
            var order = Order.builder()
                    .orderStatus(OrderStatus.CREATED)
                    .user(owner)
                    .build();
            order.addOrderItem(orderItem);
            return orderRepository.save(order);
        });

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);

        var entity = new HttpEntity<>(headers);

        ResponseEntity<OrderResponse> response = this.restTemplate.exchange("/v1/orders", HttpMethod.DELETE, entity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isEqualTo(savedOrder.getId());
        Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        Assertions.assertThat(body.getCreatedAt()).isNotNull();
        var expectedTotalPrice = BigDecimal.valueOf(quantity).multiply(productPrice);
        Assertions.assertThat(body.getTotalPrice()).isEqualByComparingTo(expectedTotalPrice);
        Assertions.assertThat(body.getOrderItems())
                .singleElement()
                .satisfies(orderItem -> {
                    Assertions.assertThat(orderItem.getProductName()).isEqualTo(productName);
                    Assertions.assertThat(orderItem.getQuantity()).isEqualTo(quantity);
                    Assertions.assertThat(orderItem.getPurchasePrice()).isEqualByComparingTo(productPrice);
                });

        transactionTemplate.executeWithoutResult(_ -> {
            var persistedProduct = productRepository.findById(savedProduct.getId())
                    .orElseThrow();
            Assertions.assertThat(persistedProduct.getStock()).isEqualTo(stock);
            Assertions.assertThat(persistedProduct.getReservedStock()).isEqualTo(reservedStock - quantity);

            var persistedOrder = orderRepository.findById(savedOrder.getId())
                    .orElseThrow();

            Assertions.assertThat(persistedOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    private void setAuthorizationHeader(HttpHeaders headers) {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername(NORMAL_USERNAME);
        loginRequest.setPassword(NORMAL_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
        Assertions.assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(loginResponse.getBody()).isNotNull();

        String jwtToken = loginResponse.getBody().token();
        Assertions.assertThat(jwtToken).isNotBlank();
        headers.setBearerAuth(jwtToken);
    }
}
