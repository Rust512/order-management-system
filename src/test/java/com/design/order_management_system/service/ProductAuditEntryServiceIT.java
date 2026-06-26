package com.design.order_management_system.service;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.documentation.schema.PagedProductAuditEntryResponse;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.ProductAuditEntryResponse;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import java.math.BigDecimal;
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
class ProductAuditEntryServiceIT extends DatabaseTest {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;

  private static final String ADMIN_USERNAME = GeneratorUtils.generateUUID();
  private static final String ADMIN_PASSWORD = "ADM@4103";
  @Autowired private TransactionTemplate transactionTemplate;

  @BeforeAll
  void setUp() {
    var adminRole =
        roleRepository
            .findByName(CommonConstants.ROLE_ADMIN)
            .orElseThrow(
                () -> new IllegalStateException(ErrorMessageConstants.ROLE_USER_WAS_NOT_SEEDED));
    var adminUser =
        User.builder()
            .username(ADMIN_USERNAME)
            .password(passwordEncoder.encode(ADMIN_PASSWORD))
            .build();
    adminUser.addRole(adminRole);
    userRepository.save(adminUser);
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/products/{productId}/auditLogs API should return all the audit logs
            corresponding to the given product ID.
            """)
  void getProductVersions_ShouldReturnAllAuditEntriesForTheGivenProductId() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var productId = response.getBody().getProductId();
    Assertions.assertThat(productId).isNotNull();

    var newProductName = GeneratorUtils.generateUUID();
    var newPrice = BigDecimal.TEN;
    var stockToAdd = 3L;

    var updateProductRequest =
        ProductUpdateRequest.builder()
            .newProductName(newProductName)
            .updatedPrice(newPrice)
            .stockToAdd(stockToAdd)
            .build();

    var updateRequestEntity = new HttpEntity<>(updateProductRequest, headers);

    var uri =
        UriComponentsBuilder.fromUriString("/v1/products/{productId}")
            .buildAndExpand(productId)
            .toUri();

    var productUpdateResponse =
        restTemplate.exchange(uri, HttpMethod.PUT, updateRequestEntity, ProductResponse.class);

    Assertions.assertThat(productUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(productUpdateResponse.getBody()).isNotNull();

    var page = 0;
    var size = 1;
    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/products/{productId}/audit")
            .queryParam("page", page)
            .queryParam("size", size)
            .buildAndExpand(productId)
            .toUri();

    var auditLogRequestEntity = new HttpEntity<>(headers);
    var auditLogResponse =
        restTemplate.exchange(
            auditLogsUri,
            HttpMethod.GET,
            auditLogRequestEntity,
            PagedProductAuditEntryResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var pagedAuditLogs = auditLogResponse.getBody();
    Assertions.assertThat(pagedAuditLogs.getContent()).hasSize(1);
    Assertions.assertThat(pagedAuditLogs.getPage()).isEqualTo(page);
    Assertions.assertThat(pagedAuditLogs.getSize()).isEqualTo(size);
    Assertions.assertThat(pagedAuditLogs.getTotalElements()).isEqualTo(2);
    Assertions.assertThat(pagedAuditLogs.getTotalPages()).isEqualTo(2);
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/products/{productId}/auditLogs/{version} API should return the audit log to the
            corresponding version and the given product ID if it exists
            """)
  void
      getProductAuditEntryByVersion_WhenAuditEntryWithGivenVersionExists_ShouldReturnTheSameAuditEntry() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var productId = response.getBody().getProductId();
    Assertions.assertThat(productId).isNotNull();

    var newProductName = GeneratorUtils.generateUUID();
    var newPrice = BigDecimal.TEN;
    var stockToAdd = 3L;
    var expectedStock = stock + stockToAdd;

    var updateProductRequest =
        ProductUpdateRequest.builder()
            .newProductName(newProductName)
            .updatedPrice(newPrice)
            .stockToAdd(stockToAdd)
            .build();

    var updateRequestEntity = new HttpEntity<>(updateProductRequest, headers);

    var uri =
        UriComponentsBuilder.fromUriString("/v1/products/{productId}")
            .buildAndExpand(productId)
            .toUri();

    var productUpdateResponse =
        restTemplate.exchange(uri, HttpMethod.PUT, updateRequestEntity, ProductResponse.class);

    Assertions.assertThat(productUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(productUpdateResponse.getBody()).isNotNull();

    var page = 0;
    var size = 1;
    var version = 2;
    var auditLogsUri =
        UriComponentsBuilder.fromUriString("/v1/products/{productId}/audit/{version}")
            .queryParam("page", page)
            .queryParam("size", size)
            .buildAndExpand(productId, version)
            .toUri();

    var auditLogRequestEntity = new HttpEntity<>(headers);
    var auditLogResponse =
        restTemplate.exchange(
            auditLogsUri, HttpMethod.GET, auditLogRequestEntity, ProductAuditEntryResponse.class);

    Assertions.assertThat(auditLogResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(auditLogResponse.getBody()).isNotNull();

    var auditLog = auditLogResponse.getBody();
    Assertions.assertThat(auditLog.version()).isEqualTo(version);
    Assertions.assertThat(auditLog.productName()).isEqualTo(newProductName);
    Assertions.assertThat(auditLog.price()).isEqualByComparingTo(newPrice);
    Assertions.assertThat(auditLog.stock()).isEqualTo(expectedStock);
    Assertions.assertThat(auditLog.operationType()).isEqualTo(OperationType.UPDATE);
    Assertions.assertThat(auditLog.changedByUserId()).isNotNull();
    Assertions.assertThat(auditLog.changedByUsername()).isEqualTo(ADMIN_USERNAME);
    Assertions.assertThat(auditLog.createdAt()).isNotNull();
  }

  private void setAuthorizationHeader(HttpHeaders headers) {
    var loginRequest = new LoginRequest();
    loginRequest.setUsername(ProductAuditEntryServiceIT.ADMIN_USERNAME);
    loginRequest.setPassword(ProductAuditEntryServiceIT.ADMIN_PASSWORD);

    ResponseEntity<LoginResponse> loginResponse =
        this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
    Assertions.assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(loginResponse.getBody()).isNotNull();

    String jwtToken = loginResponse.getBody().token();
    Assertions.assertThat(jwtToken).isNotBlank();
    headers.setBearerAuth(jwtToken);
  }
}
