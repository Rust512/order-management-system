package com.design.order_management_system.service;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.OrderAuditEntryResponse;
import com.design.order_management_system.dto.response.OrderResponse;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderOperation;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import java.math.BigDecimal;
import java.util.List;
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

@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderAuditEntryServiceIT extends DatabaseTest {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private OrderRepository orderRepository;

  private static final String ADMIN_USERNAME = GeneratorUtils.generateUUID();
  private static final String ADMIN_PASSWORD = "ADM@4103";
  private static final String NORMAL_USERNAME = GeneratorUtils.generateUUID();
  private static final String NORMAL_PASSWORD = "NRL@5896";

  @BeforeAll
  void beforeAll() {
    var adminRole =
        roleRepository
            .findByName(CommonConstants.ROLE_ADMIN)
            .orElseThrow(
                () -> new IllegalStateException(ErrorMessageConstants.ROLE_USER_WAS_NOT_SEEDED));
    var normalRole =
        roleRepository
            .findByName(CommonConstants.ROLE_USER)
            .orElseThrow(
                () -> new IllegalStateException(ErrorMessageConstants.ROLE_USER_WAS_NOT_SEEDED));
    var adminUser =
        User.builder()
            .username(ADMIN_USERNAME)
            .password(passwordEncoder.encode(ADMIN_PASSWORD))
            .build();
    adminUser.addRole(adminRole);
    var normalUser =
        User.builder()
            .username(NORMAL_USERNAME)
            .password(passwordEncoder.encode(NORMAL_PASSWORD))
            .build();
    normalUser.addRole(normalRole);
    userRepository.saveAll(List.of(adminUser, normalUser));
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/orders/{orderId}/audit should return all audit entries for the given order ID
            regardless of ownership, if the logged-in user is an ADMIN.
            """)
  void getOrderAuditEntries_WithAdminUser_ShouldReturnOrderAuditEntries() {
    String productName = "Prd01";
    BigDecimal productPrice = BigDecimal.TEN;
    long stock = 6L;
    long reservedStock = 0L;

    var product =
        productRepository.save(
            Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
    var productId = product.getId();
    var quantity = 2L;

    var orderItemRequest =
        OrderItemRequest.builder().productId(productId).quantity(quantity).build();

    var headers = new HttpHeaders();
    setAuthorizationHeader(headers, NORMAL_USERNAME, NORMAL_PASSWORD);
    var requestEntity = new HttpEntity<>(orderItemRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getOrderId()).isNotNull();
    var orderId = body.getOrderId();
    validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

    var updatedQuantity = 3L;
    var updateRequestBody =
        OrderItemRequest.builder().productId(productId).quantity(updatedQuantity).build();

    var updateRequest = new HttpEntity<>(updateRequestBody, headers);
    ResponseEntity<OrderResponse> updatedResponse =
        restTemplate.exchange(
            "/v1/orders/items", HttpMethod.PUT, updateRequest, OrderResponse.class);

    Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(updatedResponse.getBody()).isNotNull();
    var updatedResponseBody = updatedResponse.getBody();
    Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
    validateOrderResponse(
        updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);

    var adminHeaders = new HttpHeaders();
    setAuthorizationHeader(adminHeaders, ADMIN_USERNAME, ADMIN_PASSWORD);

    var auditEntryRequest = new HttpEntity<>(adminHeaders);

    var page = 0;
    var size = 2;

    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/orders/{orderId}/audit")
            .queryParam("page", page)
            .queryParam("size", size)
            .buildAndExpand(orderId)
            .toUri();

    var auditLogResponse =
        restTemplate.exchange(auditLogsUri, HttpMethod.GET, auditEntryRequest, PagedResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var pagedAuditLogs = auditLogResponse.getBody();
    Assertions.assertThat(pagedAuditLogs.getContent()).hasSize(size);
    Assertions.assertThat(pagedAuditLogs.getPage()).isEqualTo(page);
    Assertions.assertThat(pagedAuditLogs.getSize()).isEqualTo(size);
    Assertions.assertThat(pagedAuditLogs.getTotalElements()).isEqualTo(3);
    Assertions.assertThat(pagedAuditLogs.getTotalPages()).isEqualTo(2);
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/orders/{orderId}/audit should return all audit entries for the given order ID,
            if the logged-in user is not an ADMIN and they own the order.
            """)
  void getOrderAuditEntries_WithNormalUser_ShouldReturnOrderAuditEntriesWhenOrderOwned() {
    String productName = "Prd01";
    BigDecimal productPrice = BigDecimal.TEN;
    long stock = 6L;
    long reservedStock = 0L;

    var product =
        productRepository.save(
            Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
    var productId = product.getId();
    var quantity = 2L;

    var orderItemRequest =
        OrderItemRequest.builder().productId(productId).quantity(quantity).build();

    var headers = new HttpHeaders();
    setAuthorizationHeader(headers, NORMAL_USERNAME, NORMAL_PASSWORD);
    var requestEntity = new HttpEntity<>(orderItemRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getOrderId()).isNotNull();
    var orderId = body.getOrderId();
    validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

    var updatedQuantity = 3L;
    var updateRequestBody =
        OrderItemRequest.builder().productId(productId).quantity(updatedQuantity).build();

    var updateRequest = new HttpEntity<>(updateRequestBody, headers);
    ResponseEntity<OrderResponse> updatedResponse =
        restTemplate.exchange(
            "/v1/orders/items", HttpMethod.PUT, updateRequest, OrderResponse.class);

    Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(updatedResponse.getBody()).isNotNull();
    var updatedResponseBody = updatedResponse.getBody();
    Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
    validateOrderResponse(
        updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);

    var adminHeaders = new HttpHeaders();
    setAuthorizationHeader(adminHeaders, NORMAL_USERNAME, NORMAL_PASSWORD);

    var auditEntryRequest = new HttpEntity<>(adminHeaders);

    var page = 0;
    var size = 2;

    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/orders/{orderId}/audit")
            .queryParam("page", page)
            .queryParam("size", size)
            .buildAndExpand(orderId)
            .toUri();

    var auditLogResponse =
        restTemplate.exchange(auditLogsUri, HttpMethod.GET, auditEntryRequest, PagedResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var pagedAuditLogs = auditLogResponse.getBody();
    Assertions.assertThat(pagedAuditLogs.getContent()).hasSize(size);
    Assertions.assertThat(pagedAuditLogs.getPage()).isEqualTo(page);
    Assertions.assertThat(pagedAuditLogs.getSize()).isEqualTo(size);
    Assertions.assertThat(pagedAuditLogs.getTotalElements()).isEqualTo(3);
    Assertions.assertThat(pagedAuditLogs.getTotalPages()).isEqualTo(2);
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/orders/{orderId}/audit should return all audit entries for the given order ID,
            if the logged-in user is not an ADMIN and they own the order.
            """)
  void getOrderAuditEntries_WithNormalUser_ShouldNotReturnOrderAuditEntriesWhenOrderNotOwned() {
    String productName = "Prd01";
    BigDecimal productPrice = BigDecimal.TEN;
    long stock = 6L;
    long reservedStock = 0L;

    var product =
        productRepository.save(
            Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
    var productId = product.getId();
    var quantity = 2L;

    var orderItemRequest =
        OrderItemRequest.builder().productId(productId).quantity(quantity).build();

    var headers = new HttpHeaders();
    setAuthorizationHeader(headers, ADMIN_USERNAME, ADMIN_PASSWORD);
    var requestEntity = new HttpEntity<>(orderItemRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getOrderId()).isNotNull();
    var orderId = body.getOrderId();
    validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

    var updatedQuantity = 3L;
    var updateRequestBody =
        OrderItemRequest.builder().productId(productId).quantity(updatedQuantity).build();

    var updateRequest = new HttpEntity<>(updateRequestBody, headers);
    ResponseEntity<OrderResponse> updatedResponse =
        restTemplate.exchange(
            "/v1/orders/items", HttpMethod.PUT, updateRequest, OrderResponse.class);

    Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(updatedResponse.getBody()).isNotNull();
    var updatedResponseBody = updatedResponse.getBody();
    Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
    validateOrderResponse(
        updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);

    var adminHeaders = new HttpHeaders();
    setAuthorizationHeader(adminHeaders, NORMAL_USERNAME, NORMAL_PASSWORD);

    var auditEntryRequest = new HttpEntity<>(adminHeaders);

    var page = 0;
    var size = 2;

    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/orders/{orderId}/audit")
            .queryParam("page", page)
            .queryParam("size", size)
            .buildAndExpand(orderId)
            .toUri();

    var auditLogResponse =
        restTemplate.exchange(auditLogsUri, HttpMethod.GET, auditEntryRequest, PagedResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var pagedAuditLogs = auditLogResponse.getBody();
    Assertions.assertThat(pagedAuditLogs.getContent()).isEmpty();
    Assertions.assertThat(pagedAuditLogs.getPage()).isEqualTo(page);
    Assertions.assertThat(pagedAuditLogs.getSize()).isEqualTo(size);
    Assertions.assertThat(pagedAuditLogs.getTotalElements()).isEqualTo(0);
    Assertions.assertThat(pagedAuditLogs.getTotalPages()).isEqualTo(0);
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/orders/{orderId}/audit/{version} API should return the corresponding audit entry for the given order ID and version
            regardless of ownership, if the logged-in user is an ADMIN.
            """)
  void getOrderAuditEntriesByVersion_WithAdminUser_ShouldReturnOrderAuditEntry() {
    String productName = "Prd01";
    BigDecimal productPrice = BigDecimal.TEN;
    long stock = 6L;
    long reservedStock = 0L;

    var product =
        productRepository.save(
            Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
    var productId = product.getId();
    var quantity = 2L;

    var createdByUserId = userRepository.findByUsername(NORMAL_USERNAME).orElseThrow().getId();

    var orderItemRequest =
        OrderItemRequest.builder().productId(productId).quantity(quantity).build();

    var headers = new HttpHeaders();
    setAuthorizationHeader(headers, NORMAL_USERNAME, NORMAL_PASSWORD);
    var requestEntity = new HttpEntity<>(orderItemRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getOrderId()).isNotNull();
    var orderId = body.getOrderId();
    validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

    var updatedQuantity = 3L;
    var updateRequestBody =
        OrderItemRequest.builder().productId(productId).quantity(updatedQuantity).build();

    var updateRequest = new HttpEntity<>(updateRequestBody, headers);
    ResponseEntity<OrderResponse> updatedResponse =
        restTemplate.exchange(
            "/v1/orders/items", HttpMethod.PUT, updateRequest, OrderResponse.class);

    Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(updatedResponse.getBody()).isNotNull();
    var updatedResponseBody = updatedResponse.getBody();
    Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
    validateOrderResponse(
        updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);

    var adminHeaders = new HttpHeaders();
    setAuthorizationHeader(adminHeaders, ADMIN_USERNAME, ADMIN_PASSWORD);

    var auditEntryRequest = new HttpEntity<>(adminHeaders);

    var version = 2;

    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/orders/{orderId}/audit/{version}")
            .buildAndExpand(orderId, version)
            .toUri();

    var auditLogResponse =
        restTemplate.exchange(
            auditLogsUri, HttpMethod.GET, auditEntryRequest, OrderAuditEntryResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var orderAuditEntry = auditLogResponse.getBody();

    Assertions.assertThat(orderAuditEntry.version()).isEqualTo(version);
    Assertions.assertThat(orderAuditEntry.operation()).isEqualTo(OrderOperation.ADD_ITEM);
    Assertions.assertThat(orderAuditEntry.createdAt()).isNotNull();
    Assertions.assertThat(orderAuditEntry.changedByUserId()).isEqualTo(createdByUserId);
    Assertions.assertThat(orderAuditEntry.changedByUsername()).isEqualTo(NORMAL_USERNAME);

    var orderSnapshot = orderAuditEntry.snapshot();

    Assertions.assertThat(orderSnapshot.getOrderId()).isEqualTo(orderId);
    Assertions.assertThat(orderSnapshot.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
    var totalPrice = BigDecimal.valueOf(quantity).multiply(productPrice);
    Assertions.assertThat(orderSnapshot.getTotalPrice()).isEqualByComparingTo(totalPrice);
    Assertions.assertThat(orderSnapshot.getOrderItemSnapshots())
        .singleElement()
        .satisfies(
            orderItem -> {
              Assertions.assertThat(orderItem.getProductId()).isEqualTo(productId);
              Assertions.assertThat(orderItem.getProductName()).isEqualTo(productName);
              Assertions.assertThat(orderItem.getQuantity()).isEqualTo(quantity);
            });
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/orders/{orderId}/audit/{version} API should return the corresponding audit entry for the given order ID and version
            after checking ownership ownership, if the logged-in user is not an ADMIN.
            """)
  void getOrderAuditEntriesByVersion_WithoutAdminUser_ShouldReturnOrderAuditEntryIfOwned() {
    String productName = "Prd01";
    BigDecimal productPrice = BigDecimal.TEN;
    long stock = 6L;
    long reservedStock = 0L;

    var product =
        productRepository.save(
            Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(stock)
                .reservedStock(reservedStock)
                .build());
    var productId = product.getId();
    var quantity = 2L;

    var createdByUserId = userRepository.findByUsername(NORMAL_USERNAME).orElseThrow().getId();

    var orderItemRequest =
        OrderItemRequest.builder().productId(productId).quantity(quantity).build();

    var headers = new HttpHeaders();
    setAuthorizationHeader(headers, NORMAL_USERNAME, NORMAL_PASSWORD);
    var requestEntity = new HttpEntity<>(orderItemRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity("/v1/orders/items", requestEntity, OrderResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getOrderId()).isNotNull();
    var orderId = body.getOrderId();
    validateOrderResponse(body, quantity, productPrice, productName, productId, orderId);

    var updatedQuantity = 3L;
    var updateRequestBody =
        OrderItemRequest.builder().productId(productId).quantity(updatedQuantity).build();

    var updateRequest = new HttpEntity<>(updateRequestBody, headers);
    ResponseEntity<OrderResponse> updatedResponse =
        restTemplate.exchange(
            "/v1/orders/items", HttpMethod.PUT, updateRequest, OrderResponse.class);

    Assertions.assertThat(updatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(updatedResponse.getBody()).isNotNull();
    var updatedResponseBody = updatedResponse.getBody();
    Assertions.assertThat(updatedResponseBody.getOrderId()).isEqualTo(orderId);
    validateOrderResponse(
        updatedResponseBody, updatedQuantity, productPrice, productName, productId, orderId);

    var adminHeaders = new HttpHeaders();
    setAuthorizationHeader(adminHeaders, NORMAL_USERNAME, NORMAL_PASSWORD);

    var auditEntryRequest = new HttpEntity<>(adminHeaders);

    var version = 2;

    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/orders/{orderId}/audit/{version}")
            .buildAndExpand(orderId, version)
            .toUri();

    var auditLogResponse =
        restTemplate.exchange(
            auditLogsUri, HttpMethod.GET, auditEntryRequest, OrderAuditEntryResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var orderAuditEntry = auditLogResponse.getBody();

    Assertions.assertThat(orderAuditEntry.version()).isEqualTo(version);
    Assertions.assertThat(orderAuditEntry.operation()).isEqualTo(OrderOperation.ADD_ITEM);
    Assertions.assertThat(orderAuditEntry.createdAt()).isNotNull();
    Assertions.assertThat(orderAuditEntry.changedByUserId()).isEqualTo(createdByUserId);
    Assertions.assertThat(orderAuditEntry.changedByUsername()).isEqualTo(NORMAL_USERNAME);

    var orderSnapshot = orderAuditEntry.snapshot();

    Assertions.assertThat(orderSnapshot.getOrderId()).isEqualTo(orderId);
    Assertions.assertThat(orderSnapshot.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
    var totalPrice = BigDecimal.valueOf(quantity).multiply(productPrice);
    Assertions.assertThat(orderSnapshot.getTotalPrice()).isEqualByComparingTo(totalPrice);
    Assertions.assertThat(orderSnapshot.getOrderItemSnapshots())
        .singleElement()
        .satisfies(
            orderItem -> {
              Assertions.assertThat(orderItem.getProductId()).isEqualTo(productId);
              Assertions.assertThat(orderItem.getProductName()).isEqualTo(productName);
              Assertions.assertThat(orderItem.getQuantity()).isEqualTo(quantity);
            });
  }

  private void validateOrderResponse(
      OrderResponse body,
      long expectedQuantity,
      BigDecimal expectedPurchasePrice,
      String expectedProductName,
      Long productId,
      Long orderId) {
    Assertions.assertThat(body.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
    Assertions.assertThat(body.getCreatedAt()).isNotNull();
    var expectedTotalPrice = BigDecimal.valueOf(expectedQuantity).multiply(expectedPurchasePrice);
    Assertions.assertThat(body.getTotalPrice()).isEqualByComparingTo(expectedTotalPrice);
    Assertions.assertThat(body.getOrderItems()).hasSize(1);

    var orderItem = body.getOrderItems().getFirst();
    Assertions.assertThat(orderItem.getQuantity()).isEqualTo(expectedQuantity);
    Assertions.assertThat(orderItem.getProductName()).isEqualTo(expectedProductName);
    Assertions.assertThat(orderItem.getPurchasePrice()).isEqualTo(expectedPurchasePrice);

    transactionTemplate.executeWithoutResult(
        _ -> {
          var persistedProduct = productRepository.findById(productId).orElseThrow();
          Assertions.assertThat(persistedProduct.getReservedStock()).isEqualTo(expectedQuantity);

          var draftOrder = orderRepository.getOrderByIdWithItems(orderId).orElseThrow();
          Assertions.assertThat(draftOrder.getOrderItems()).singleElement().isNotNull();
        });
  }

  private void setAuthorizationHeader(HttpHeaders headers, String username, String password) {
    var loginRequest = new LoginRequest();
    loginRequest.setUsername(username);
    loginRequest.setPassword(password);

    ResponseEntity<LoginResponse> loginResponse =
        this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
    Assertions.assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(loginResponse.getBody()).isNotNull();

    String jwtToken = loginResponse.getBody().token();
    Assertions.assertThat(jwtToken).isNotBlank();
    headers.setBearerAuth(jwtToken);
  }
}
