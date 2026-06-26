package com.design.order_management_system.controller;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.request.ProductUpdateRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.PagedResponse;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.security.User;
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
class ProductControllerIntegrationTest extends DatabaseTest {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private ProductRepository productRepository;

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
            If the product name in the POST /v1/products request does not exist,
            the API should create a new product and return HTTP status 201.
            """)
  void registerProduct_WhenProductNameDoesNotExist_ShouldReturnStatus201() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers, ADMIN_USERNAME, ADMIN_PASSWORD);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getProductId()).isNotNull();
    Assertions.assertThat(body.getProductName()).isEqualTo(productName);
    Assertions.assertThat(body.getPrice()).isEqualByComparingTo(price);
    Assertions.assertThat(body.getStock()).isEqualTo(stock);

    transactionTemplate.executeWithoutResult(
        _ -> {
          var persistedProduct = productRepository.findById(body.getProductId()).orElseThrow();
          Assertions.assertThat(persistedProduct).isNotNull();
          Assertions.assertThat(persistedProduct.getId()).isEqualTo(body.getProductId());
          Assertions.assertThat(persistedProduct.getName()).isEqualTo(productName);
          Assertions.assertThat(persistedProduct.getPrice()).isEqualByComparingTo(price);
          Assertions.assertThat(persistedProduct.getStock()).isEqualTo(stock);
          Assertions.assertThat(persistedProduct.getReservedStock()).isZero();
          Assertions.assertThat(persistedProduct.getAvailableStock()).isEqualTo(stock);
        });
  }

  @Test
  @DisplayName(
      value =
          """
            If the product ID given in the PUT /v1/products/{id} API exists,
            the API should update the product with the given ID, and should return HTTP status 200.
            """)
  void updateProduct_WhenProductExists_ShouldUpdateProductAndReturnStatus200() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers, ADMIN_USERNAME, ADMIN_PASSWORD);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getProductId()).isNotNull();
    var productId = body.getProductId();

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

    var updateBody = productUpdateResponse.getBody();

    Assertions.assertThat(updateBody.getProductId()).isEqualTo(productId);
    Assertions.assertThat(updateBody.getProductId()).isNotNull();
    Assertions.assertThat(updateBody.getProductName()).isEqualTo(newProductName);
    Assertions.assertThat(updateBody.getPrice()).isEqualByComparingTo(newPrice);
    var expectedStock = stock + stockToAdd;
    Assertions.assertThat(updateBody.getStock()).isEqualTo(expectedStock);

    transactionTemplate.executeWithoutResult(
        _ -> {
          var persistedProduct =
              productRepository.findById(updateBody.getProductId()).orElseThrow();
          Assertions.assertThat(persistedProduct).isNotNull();
          Assertions.assertThat(persistedProduct.getId()).isEqualTo(updateBody.getProductId());
          Assertions.assertThat(persistedProduct.getName()).isEqualTo(newProductName);
          Assertions.assertThat(persistedProduct.getPrice()).isEqualByComparingTo(newPrice);
          Assertions.assertThat(persistedProduct.getStock()).isEqualTo(expectedStock);
          Assertions.assertThat(persistedProduct.getReservedStock()).isZero();
          Assertions.assertThat(persistedProduct.getAvailableStock()).isEqualTo(expectedStock);
        });
  }

  @Test
  @DisplayName(
      value =
          """
            If the product ID given in the PUT /v1/products/{id} API exists,
            and no new product name is provided,
            the API should not update the product name, and should return HTTP status 200.
            """)
  void updateProduct_WhenNewProductNameNotGiven_ShouldNotUpdateProductNameAndReturnStatus200() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers, ADMIN_USERNAME, ADMIN_PASSWORD);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getProductId()).isNotNull();
    var productId = body.getProductId();

    var newPrice = BigDecimal.TEN;
    var stockToAdd = 3L;

    var updateProductRequest =
        ProductUpdateRequest.builder().updatedPrice(newPrice).stockToAdd(stockToAdd).build();

    var updateRequestEntity = new HttpEntity<>(updateProductRequest, headers);

    var uri =
        UriComponentsBuilder.fromUriString("/v1/products/{productId}")
            .buildAndExpand(productId)
            .toUri();

    var productUpdateResponse =
        restTemplate.exchange(uri, HttpMethod.PUT, updateRequestEntity, ProductResponse.class);

    Assertions.assertThat(productUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(productUpdateResponse.getBody()).isNotNull();

    var updateBody = productUpdateResponse.getBody();

    Assertions.assertThat(updateBody.getProductId()).isEqualTo(productId);
    Assertions.assertThat(updateBody.getProductId()).isNotNull();
    Assertions.assertThat(updateBody.getProductName()).isEqualTo(productName);
    Assertions.assertThat(updateBody.getPrice()).isEqualByComparingTo(newPrice);
    var expectedStock = stock + stockToAdd;
    Assertions.assertThat(updateBody.getStock()).isEqualTo(expectedStock);

    transactionTemplate.executeWithoutResult(
        _ -> {
          var persistedProduct =
              productRepository.findById(updateBody.getProductId()).orElseThrow();
          Assertions.assertThat(persistedProduct).isNotNull();
          Assertions.assertThat(persistedProduct.getId()).isEqualTo(updateBody.getProductId());
          Assertions.assertThat(persistedProduct.getName()).isEqualTo(productName);
          Assertions.assertThat(persistedProduct.getPrice()).isEqualByComparingTo(newPrice);
          Assertions.assertThat(persistedProduct.getStock()).isEqualTo(expectedStock);
          Assertions.assertThat(persistedProduct.getReservedStock()).isZero();
          Assertions.assertThat(persistedProduct.getAvailableStock()).isEqualTo(expectedStock);
        });
  }

  @Test
  @DisplayName(
      value =
          """
            If the product ID given in the PUT /v1/products/{id} API exists,
            and no new product price is provided,
            the API should not update the product price, and should return HTTP status 200.
            """)
  void updateProduct_WhenNewProductPriceNotGiven_ShouldNotUpdateProductPriceAndReturnStatus200() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers, ADMIN_USERNAME, ADMIN_PASSWORD);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getProductId()).isNotNull();
    var productId = body.getProductId();

    var newProductName = GeneratorUtils.generateUUID();
    var stockToAdd = 3L;

    var updateProductRequest =
        ProductUpdateRequest.builder()
            .newProductName(newProductName)
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

    var updateBody = productUpdateResponse.getBody();

    Assertions.assertThat(updateBody.getProductId()).isEqualTo(productId);
    Assertions.assertThat(updateBody.getProductId()).isNotNull();
    Assertions.assertThat(updateBody.getProductName()).isEqualTo(newProductName);
    Assertions.assertThat(updateBody.getPrice()).isEqualByComparingTo(price);
    var expectedStock = stock + stockToAdd;
    Assertions.assertThat(updateBody.getStock()).isEqualTo(expectedStock);

    transactionTemplate.executeWithoutResult(
        _ -> {
          var persistedProduct =
              productRepository.findById(updateBody.getProductId()).orElseThrow();
          Assertions.assertThat(persistedProduct).isNotNull();
          Assertions.assertThat(persistedProduct.getId()).isEqualTo(updateBody.getProductId());
          Assertions.assertThat(persistedProduct.getName()).isEqualTo(newProductName);
          Assertions.assertThat(persistedProduct.getPrice()).isEqualByComparingTo(price);
          Assertions.assertThat(persistedProduct.getStock()).isEqualTo(expectedStock);
          Assertions.assertThat(persistedProduct.getReservedStock()).isZero();
          Assertions.assertThat(persistedProduct.getAvailableStock()).isEqualTo(expectedStock);
        });
  }

  @Test
  @DisplayName(
      value =
          """
            If the product ID given in the PUT /v1/products/{id} API exists,
            and stock to add is not provided,
            the API should not update the product stock, and should return HTTP status 200.
            """)
  void updateProduct_WhenStockToAddNotGiven_ShouldNotUpdateProductStockAndReturnStatus200() {
    var productName = GeneratorUtils.generateUUID();
    var price = BigDecimal.TWO;
    var stock = 2L;
    var request =
        CreateProductRequest.builder().productName(productName).price(price).stock(stock).build();

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers, ADMIN_USERNAME, ADMIN_PASSWORD);

    var requestEntity = new HttpEntity<>(request, headers);

    var response = restTemplate.postForEntity("/v1/products", requestEntity, ProductResponse.class);
    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getProductId()).isNotNull();
    var productId = body.getProductId();

    var newProductName = GeneratorUtils.generateUUID();
    var newPrice = BigDecimal.TEN;

    var updateProductRequest =
        ProductUpdateRequest.builder()
            .newProductName(newProductName)
            .updatedPrice(newPrice)
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

    var updateBody = productUpdateResponse.getBody();

    Assertions.assertThat(updateBody.getProductId()).isEqualTo(productId);
    Assertions.assertThat(updateBody.getProductId()).isNotNull();
    Assertions.assertThat(updateBody.getProductName()).isEqualTo(newProductName);
    Assertions.assertThat(updateBody.getPrice()).isEqualByComparingTo(newPrice);
    Assertions.assertThat(updateBody.getStock()).isEqualTo(stock);

    transactionTemplate.executeWithoutResult(
        _ -> {
          var persistedProduct =
              productRepository.findById(updateBody.getProductId()).orElseThrow();
          Assertions.assertThat(persistedProduct).isNotNull();
          Assertions.assertThat(persistedProduct.getId()).isEqualTo(updateBody.getProductId());
          Assertions.assertThat(persistedProduct.getName()).isEqualTo(newProductName);
          Assertions.assertThat(persistedProduct.getPrice()).isEqualByComparingTo(newPrice);
          Assertions.assertThat(persistedProduct.getStock()).isEqualTo(stock);
          Assertions.assertThat(persistedProduct.getReservedStock()).isZero();
          Assertions.assertThat(persistedProduct.getAvailableStock()).isEqualTo(stock);
        });
  }

  @Test
  @DisplayName(
      value =
          """
            The GET /v1/products should return all existing products
            """)
  void getProducts_ShouldReturnAllExistingProducts() {
    var productName0 = GeneratorUtils.generateUUID();
    var productName1 = GeneratorUtils.generateUUID();
    var price0 = BigDecimal.TWO;
    var price1 = BigDecimal.TEN;
    var stock0 = 2L;
    var stock1 = 3L;
    var productId0 =
        transactionTemplate.execute(
            _ -> {
              var product0 =
                  Product.builder().name(productName0).price(price0).stock(stock0).build();

              return productRepository.save(product0).getId();
            });
    var productId1 =
        transactionTemplate.execute(
            _ -> {
              var product1 =
                  Product.builder().name(productName1).price(price1).stock(stock1).build();

              return productRepository.save(product1).getId();
            });

    HttpHeaders headers = new HttpHeaders();
    setAuthorizationHeader(headers, NORMAL_USERNAME, NORMAL_PASSWORD);

    var requestEntity = new HttpEntity<>(headers);
    var page = 0;
    var size = 1;
    var uri =
        UriComponentsBuilder.fromUriString("/v1/products")
            .queryParam("page", page)
            .queryParam("size", size)
            .encode()
            .toUriString();

    var response =
        this.restTemplate.exchange(uri, HttpMethod.GET, requestEntity, PagedResponse.class);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Assertions.assertThat(response.getBody()).isNotNull();

    var body = response.getBody();
    Assertions.assertThat(body.getContent()).hasSize(1);
    Assertions.assertThat(body.getPage()).isEqualTo(page);
    Assertions.assertThat(body.getSize()).isEqualTo(size);
    Assertions.assertThat(body.getTotalElements()).isEqualTo(2);
    Assertions.assertThat(body.getTotalPages()).isEqualTo(2);
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
