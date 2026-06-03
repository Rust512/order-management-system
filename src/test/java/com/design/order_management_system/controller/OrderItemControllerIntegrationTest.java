package com.design.order_management_system.controller;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.OrderResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderItemControllerIntegrationTest extends DatabaseTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
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
    @DisplayName("""
            If the draft order corresponding to the logged in user does not exist,
            The POST /v1/orders/items API should save a new order and return HTTP status 200.
            """)
    void addOrderItem_WhenDraftOrderDoesNotExist_ShouldSaveNewOrderAndReturnStatus200() {
        String productName = "Prd01";
        BigDecimal productPrice = BigDecimal.TEN;
        long stock = 5L;
        long reservedStock = 0L;

        var product = productRepository.save(Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
        var productId = product.getId();
        var quantity = stock - 1L;

        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);
        var requestEntity = new HttpEntity<>(orderItemRequest, headers);

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isNotNull();
        Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(body.getCreatedAt()).isNotNull();
        var expectedTotalPrice = BigDecimal.valueOf(quantity).multiply(productPrice);
        Assertions.assertThat(body.getTotalPrice()).isEqualByComparingTo(expectedTotalPrice);
        Assertions.assertThat(body.getOrderItems())
                .hasSize(1);

        var orderItem = body.getOrderItems().getFirst();
        Assertions.assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        Assertions.assertThat(orderItem.getProductName()).isEqualTo(productName);
        Assertions.assertThat(orderItem.getPurchasePrice()).isEqualTo(productPrice);

        var persistedProduct = productRepository.findById(productId).orElseThrow();
        Assertions.assertThat(persistedProduct.getReservedStock()).isEqualTo(quantity);
    }


    @Test
    @DisplayName("""
            If the draft order corresponding to the logged in user exists and contains an order item with the same product ID as in the request,
            The POST /v1/orders/items API should update the existing order item and return HTTP status 200.
            """)
    void addOrderItem_WhenDraftOrderWithAnOrderItemWithGivenProductIdExists_ShouldUpdateTheExistingOrderItemAndReturnStatus200() {
        String productName = "Prd01";
        BigDecimal productPrice = BigDecimal.TEN;
        long stock = 6L;
        long reservedStock = 0L;

        var product = productRepository.save(Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
        var productId = product.getId();
        var quantity = 2L;

        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);
        var requestEntity = new HttpEntity<>(orderItemRequest, headers);

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isNotNull();
        var orderId = body.getOrderId();
        validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

        var additionalQuantity = 3L;
        var updatedRequestBody = OrderItemRequest.builder()
                .productId(productId)
                .quantity(additionalQuantity)
                .build();

        var updatedRequest = new HttpEntity<>(updatedRequestBody, headers);

        ResponseEntity<OrderResponse> updatedResponse = restTemplate.postForEntity("/v1/orders/items", updatedRequest, OrderResponse.class);
        Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(updatedResponse.getBody()).isNotNull();
        var updatedResponseBody = updatedResponse.getBody();
        Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
        validateOrderResponse(updatedResponseBody, quantity + additionalQuantity, productPrice, productName, productId, orderId);
    }

    @Test
    @DisplayName("""
            If the draft order corresponding to the logged in user exists, and contains an order item with the same product ID as in the request,
            The PUT /v1/orders/items/{productId} API should update the existing order item, reserve more of the stock of product with the given product ID
            and return HTTP status 200.
            """)
    void editOrderItem_WhenQuantityIncreased_ShouldReserveMoreStockAndReturnStatus200() {
        String productName = "Prd01";
        BigDecimal productPrice = BigDecimal.TEN;
        long stock = 6L;
        long reservedStock = 0L;

        var product = productRepository.save(Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
        var productId = product.getId();
        var quantity = 2L;

        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);
        var requestEntity = new HttpEntity<>(orderItemRequest, headers);


        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isNotNull();
        var orderId = body.getOrderId();
        validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

        var updatedQuantity = 3L;
        var updateRequestBody = OrderItemRequest.builder()
                .productId(productId)
                .quantity(updatedQuantity)
                .build();

        var updateRequest = new HttpEntity<>(updateRequestBody, headers);
        URI uri = UriComponentsBuilder.fromUriString("/v1/orders/items/{productId}").buildAndExpand(productId).toUri();
        ResponseEntity<OrderResponse> updatedResponse = restTemplate.exchange(uri, HttpMethod.PUT, updateRequest, OrderResponse.class);

        Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(updatedResponse.getBody()).isNotNull();
        var updatedResponseBody = updatedResponse.getBody();
        Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
        validateOrderResponse(updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);
    }

    @Test
    @DisplayName("""
            If the PUT /v1/orders/items/{productId} API requests less quantity of a product with the given product ID than before
            The API should update the existing order item, release reserved stock of product with the given product ID
            and return HTTP status 200.
            """)
    void editOrderItem_WhenQuantityDecreased_ShouldReleaseReserveStockAndReturnStatus200() {
        String productName = "Prd01";
        BigDecimal productPrice = BigDecimal.TEN;
        long stock = 6L;
        long reservedStock = 0L;

        var product = productRepository.save(Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
        var productId = product.getId();
        var quantity = 4L;

        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);
        var requestEntity = new HttpEntity<>(orderItemRequest, headers);


        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isNotNull();
        var orderId = body.getOrderId();
        validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

        var updatedQuantity = 2L;
        var updateRequestBody = OrderItemRequest.builder()
                .productId(productId)
                .quantity(updatedQuantity)
                .build();

        var updateRequest = new HttpEntity<>(updateRequestBody, headers);
        URI uri = UriComponentsBuilder.fromUriString("/v1/orders/items/{productId}").buildAndExpand(productId).toUri();
        ResponseEntity<OrderResponse> updatedResponse = restTemplate.exchange(uri, HttpMethod.PUT, updateRequest, OrderResponse.class);

        Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(updatedResponse.getBody()).isNotNull();
        var updatedResponseBody = updatedResponse.getBody();
        Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
        validateOrderResponse(updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);
    }

    @Test
    @DisplayName("""
            If the DELETE /v1/orders/items/{productId} API, with a valid product ID,
            should delete the order item corresponding to the given product ID and return HTTP status 200.
            """)
    void deleteOrderItem_WithValidProductId_ShouldDeleteOrderItemAndReturnStatus200() {
        String productName = "Prd01";
        BigDecimal productPrice = BigDecimal.TEN;
        long stock = 6L;
        long reservedStock = 0L;

        var product = productRepository.save(Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
        var productId = product.getId();
        var quantity = 4L;

        var orderItemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        var headers = new HttpHeaders();
        setAuthorizationHeader(headers);
        var requestEntity = new HttpEntity<>(orderItemRequest, headers);


        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();
        Assertions.assertThat(body.getOrderId()).isNotNull();
        var orderId = body.getOrderId();
        validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);


        var updateRequest = new HttpEntity<>(headers);
        URI uri = UriComponentsBuilder.fromUriString("/v1/orders/items/{productId}").buildAndExpand(productId).toUri();
        ResponseEntity<OrderResponse> updatedResponse = restTemplate.exchange(uri, HttpMethod.DELETE, updateRequest, OrderResponse.class);

        Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(updatedResponse.getBody()).isNotNull();
        var updatedResponseBody = updatedResponse.getBody();
        Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);

        Assertions.assertThat(updatedResponseBody.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(updatedResponseBody.getCreatedAt()).isNotNull();
        Assertions.assertThat(updatedResponseBody.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        Assertions.assertThat(updatedResponseBody.getOrderItems()).isEmpty();

        transactionTemplate.executeWithoutResult(_ -> {
            var persistedProduct = productRepository.findById(productId).orElseThrow();
            Assertions.assertThat(persistedProduct.getReservedStock()).isZero();

            var draftOrder = orderRepository.getOrderByIdWithItems(orderId).orElseThrow();
            Assertions.assertThat(draftOrder.getOrderItems()).isEmpty();
        });
    }

    private void validateOrderResponse(OrderResponse body,
                                       long expectedQuantity,
                                       BigDecimal expectedPurchasePrice,
                                       String expectedProductName,
                                       Long productId,
                                       Long orderId) {
        Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(body.getCreatedAt()).isNotNull();
        var expectedTotalPrice = BigDecimal.valueOf(expectedQuantity).multiply(expectedPurchasePrice);
        Assertions.assertThat(body.getTotalPrice()).isEqualByComparingTo(expectedTotalPrice);
        Assertions.assertThat(body.getOrderItems())
                .hasSize(1);

        var orderItem = body.getOrderItems().getFirst();
        Assertions.assertThat(orderItem.getQuantity()).isEqualTo(expectedQuantity);
        Assertions.assertThat(orderItem.getProductName()).isEqualTo(expectedProductName);
        Assertions.assertThat(orderItem.getPurchasePrice()).isEqualTo(expectedPurchasePrice);

        transactionTemplate.executeWithoutResult(_ -> {
            var persistedProduct = productRepository.findById(productId).orElseThrow();
            Assertions.assertThat(persistedProduct.getReservedStock()).isEqualTo(expectedQuantity);

            var draftOrder = orderRepository.getOrderByIdWithItems(orderId).orElseThrow();
            Assertions.assertThat(draftOrder.getOrderItems())
                    .singleElement()
                    .isNotNull();
        });
    }

    private void setAuthorizationHeader(HttpHeaders headers) {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername(OrderItemControllerIntegrationTest.NORMAL_USERNAME);
        loginRequest.setPassword(OrderItemControllerIntegrationTest.NORMAL_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
        Assertions.assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(loginResponse.getBody()).isNotNull();

        String jwtToken = loginResponse.getBody().token();
        Assertions.assertThat(jwtToken).isNotBlank();
        headers.setBearerAuth(jwtToken);
    }
}
