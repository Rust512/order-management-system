package com.design.order_management_system.controller;

import com.design.order_management_system.config.seeder.RoleSeeder;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "A";
    private static final String ADMIN_PASSWORD = "ADM@4103";
    private static final String NORMAL_USERNAME = "B";
    private static final String NORMAL_PASSWORD = "NRL@5896";

    @BeforeAll
    void beforeAll() {
        var adminRole = roleRepository.findByName(CommonConstants.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException(RoleSeeder.ROLE_USER_WAS_NOT_SEEDED));
        var normalRole = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(RoleSeeder.ROLE_USER_WAS_NOT_SEEDED));
        var adminUser = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .build();
        adminUser.addRole(adminRole);
        var normalUser = User.builder()
                .username(NORMAL_USERNAME)
                .password(passwordEncoder.encode(NORMAL_PASSWORD))
                .build();
        normalUser.addRole(normalRole);
        userRepository.saveAll(List.of(adminUser, normalUser));
    }

    @Test
    @DisplayName(value = """
            the POST /v1/user API, with an admin token
            should respond with HTTP status 201 CREATED
            """)
    void registerUser_WithAdminToken_ShouldReturnStatus201() {
        HttpHeaders headers = new HttpHeaders();
        setAuthorizationHeader(ADMIN_USERNAME, ADMIN_PASSWORD, headers);

        String username1 = "U1";
        String password1 = "P1";
        var createUserRequest = new CreateUserRequest(username1, password1);
        HttpEntity<CreateUserRequest> createUserRequestEntity = new HttpEntity<>(createUserRequest, headers);

        ResponseEntity<UserResponse> createUserResponse = this.restTemplate.postForEntity("/v1/user", createUserRequestEntity, UserResponse.class);
        Assertions.assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(createUserResponse.getBody()).isNotNull();
        var body = createUserResponse.getBody();
        Assertions.assertThat(body.getUsername()).isEqualTo(username1);
        Assertions.assertThat(body.getRoles()).containsExactly(CommonConstants.ROLE_USER);
    }

    @Test
    @DisplayName(value = """
            the POST /v1/user API, with a normal user token
            should respond with HTTP status 403 FORBIDDEN
            """)
    void registerUser_WithUserToken_ShouldReturnStatus403() {
        HttpHeaders headers = new HttpHeaders();
        setAuthorizationHeader(NORMAL_USERNAME, NORMAL_PASSWORD, headers);

        String username1 = "U1";
        String password1 = "P1";
        var createUserRequest = new CreateUserRequest(username1, password1);
        HttpEntity<CreateUserRequest> createUserRequestEntity = new HttpEntity<>(createUserRequest, headers);

        ResponseEntity<ApiErrorResponse> createUserResponse = this.restTemplate.postForEntity("/v1/user", createUserRequestEntity, ApiErrorResponse.class);
        Assertions.assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        Assertions.assertThat(createUserResponse.getBody()).isNotNull();

        var body = createUserResponse.getBody();
        Assertions.assertThat(body.getExceptionName()).isEqualTo(AuthorizationDeniedException.class.getSimpleName());
    }

    // TODO: this test failed. Debug this.
    @Test
    @DisplayName(value = """
            the POST /v1/user API, without a token
            should respond with HTTP status 401 UNAUTHORIZED
            """)
    void registerUser_WithoutToken_ShouldReturnStatus401() {
        String username1 = "U1";
        String password1 = "P1";
        var createUserRequest = new CreateUserRequest(username1, password1);

        ResponseEntity<ApiErrorResponse> createUserResponse = this.restTemplate.postForEntity("/v1/user", createUserRequest, ApiErrorResponse.class);
        Assertions.assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Assertions.assertThat(createUserResponse.getBody()).isNotNull();

        var body = createUserResponse.getBody();
        Assertions.assertThat(body.getExceptionName()).isEqualTo(AuthorizationDeniedException.class.getSimpleName());
    }

    private void setAuthorizationHeader(String username, String password, HttpHeaders headers) {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        ResponseEntity<LoginResponse> loginResponse = this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
        Assertions.assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(loginResponse.getBody()).isNotNull();

        String jwtToken = loginResponse.getBody().token();
        Assertions.assertThat(jwtToken).isNotBlank();
        headers.setBearerAuth(jwtToken);
    }
}