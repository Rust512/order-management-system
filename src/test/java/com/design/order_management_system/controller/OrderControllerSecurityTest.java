package com.design.order_management_system.controller;

import com.design.order_management_system.config.seeder.DataSeeder;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.request.OrderRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import io.jsonwebtoken.MalformedJwtException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerSecurityTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    private long productId0;
    private long productId1;
    private long stock0;
    private long stock1;
    private long userId;

    private static final String NORMAL_USERNAME = "B";
    private static final String NORMAL_PASSWORD = "NRL@5896";

    @BeforeAll
    void beforeAll() {
        var normalRole = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(DataSeeder.ROLE_USER_WAS_NOT_SEEDED));
        var normalUser = User.builder()
                .username(NORMAL_USERNAME)
                .password(passwordEncoder.encode(NORMAL_PASSWORD))
                .build();
        normalUser.addRole(normalRole);
        var savedUser = userRepository.save(normalUser);
        userId = savedUser.getId();
    }

    @AfterAll
    void afterAll() {
        userRepository.deleteById(userId);
    }

    @BeforeEach
    void setUp() {
        String productName0 = "Pr0";
        String productName1 = "Pr1";
        stock0 = 4L;
        stock1 = 3L;
        BigDecimal price0 = BigDecimal.valueOf(20.0);
        BigDecimal price1 = BigDecimal.valueOf(30.0);

        var savedProduct0 = productRepository.save(Product.builder()
                .name(productName0)
                .price(price0)
                .stock(stock0)
                .build());

        var savedProduct1 = productRepository.save(Product.builder()
                .name(productName1)
                .price(price1)
                .stock(stock1)
                .build());

        productId0 = savedProduct0.getId();
        productId1 = savedProduct1.getId();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName(value = """
            If the POST /v1/orders API receives a valid token,
            it should return HTTP status 201 with a valid response.
            """)
    void registerOrder_WithValidToken_ShouldReturnStatus201() {
        long quantityWhenStockSufficient = Math.min(stock0, stock1);
        var orderRequest = constructOrderRequest(quantityWhenStockSufficient);

        HttpHeaders headers = new HttpHeaders();
        setAuthorizationHeader(headers);

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(orderRequest, headers), OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.getOrderId()).isNotNull();
        Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(body.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName(value = """
            If the POST /v1/orders API does not receive a token,
            it should return HTTP status 401
            """)
    void registerOrder_WithoutToken_ShouldReturnStatus401() {
        long quantityWhenStockSufficient = Math.min(stock0, stock1);
        var orderRequest = constructOrderRequest(quantityWhenStockSufficient);

        ResponseEntity<String> response = restTemplate.postForEntity("/v1/orders", orderRequest, String.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Assertions.assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName(value = """
            If the POST /v1/orders API receives an invalid token,
            it should return HTTP status 401
            """)
    void registerOrder_WithInvalidToken_ShouldReturnStatus401() {
        long quantityWhenStockSufficient = Math.min(stock0, stock1);
        var orderRequest = constructOrderRequest(quantityWhenStockSufficient);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("something");

        ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(orderRequest, headers), ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        var body = response.getBody();
        Assertions.assertThat(body).isNotNull();
        Assertions.assertThat(body.getExceptionName()).isEqualTo(MalformedJwtException.class.getSimpleName());
    }

    @Test
    @DisplayName(value = """
            If the request body of the POST /v1/orders API has an order item with
            quantity more than the available stock for the corresponding product,
            it should return HTTP status 422 with a valid error response.
            """)
    void registerOrder_WithInsufficientStock_ShouldReturnStatus422() {
        long quantityWhenStockInsufficient = Math.max(stock0, stock1) + 1L;
        var orderRequest = constructOrderRequest(quantityWhenStockInsufficient);

        HttpHeaders headers = new HttpHeaders();
        setAuthorizationHeader(headers);

        ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity("/v1/orders", new HttpEntity<>(orderRequest, headers), ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        Assertions.assertThat(response.getBody()).isNotNull();
        var body = response.getBody();
        Assertions.assertThat(body.getExceptionName()).isEqualTo(InsufficientResourcesException.class.getSimpleName());
        Assertions.assertThat(body.getMessage()).isEqualTo(
                String.format(InsufficientResourcesException.INSUFFICIENT_RESOURCES, CommonConstants.PRODUCT,
                        CommonConstants.STOCK,
                        quantityWhenStockInsufficient,
                        Math.max(stock0, stock1))
        );

        var committedProducts = productRepository.findAllById(List.of(productId0, productId1));
        Assertions.assertThat(committedProducts)
                .hasSize(2)
                .extracting(Product::getStock)
                .containsExactlyInAnyOrder(stock0, stock1);
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

    private OrderRequest constructOrderRequest(long quantity) {
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(productId0)
                .quantity(quantity)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(productId1)
                .quantity(quantity)
                .build();
        return OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();
    }
}
