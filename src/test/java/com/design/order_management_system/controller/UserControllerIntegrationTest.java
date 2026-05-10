package com.design.order_management_system.controller;

import com.design.order_management_system.config.seeder.RoleSeeder;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

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

    @AfterAll
    void afterAll() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName(value = """
            the POST /v1/users API, with an admin admin token
            should respond with HTTP status 200 OK
            """)
    void registerUser_WithAdminToken_ShouldReturnStatus200() {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USERNAME);
        loginRequest.setPassword(ADMIN_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
        Assertions.assertThat(loginResponse.getBody()).isNotNull();
        String jwtToken = loginResponse.getBody().token();

        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", String.format("Bearer %s", jwtToken));

        String username1 = "U1";
        String password1 = "U1";
        var createUserRequest = new CreateUserRequest(username1, password1);
        HttpEntity<CreateUserRequest> createUserRequestEntity = new HttpEntity<>(createUserRequest, header);

        ResponseEntity<UserResponse> createUserResponse = this.restTemplate.postForEntity("/v1/user", createUserRequestEntity, UserResponse.class);
        Assertions.assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(createUserResponse.getBody()).isNotNull();
        var body = createUserResponse.getBody();
        Assertions.assertThat(body.getUsername()).isEqualTo(username1);
        Assertions.assertThat(body.getRoles()).containsExactly(CommonConstants.ROLE_USER);
    }
}