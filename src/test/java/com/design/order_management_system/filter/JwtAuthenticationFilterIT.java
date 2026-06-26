package com.design.order_management_system.filter;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.dto.common.ApiErrorResponse;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.exception.RevokedTokenException;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import io.jsonwebtoken.MalformedJwtException;
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
import org.springframework.web.util.UriComponentsBuilder;

@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtAuthenticationFilterIT extends DatabaseTest {
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;

    private static final String NORMAL_USERNAME = GeneratorUtils.generateUUID();
    private static final String NORMAL_PASSWORD = "NRL@5896";

    @BeforeAll
    void setup() {
        var normalRole =
                roleRepository
                        .findByName(CommonConstants.ROLE_USER)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessageConstants.ROLE_USER_WAS_NOT_SEEDED));
        var normalUser =
                User.builder()
                        .username(NORMAL_USERNAME)
                        .password(passwordEncoder.encode(NORMAL_PASSWORD))
                        .build();
        normalUser.addRole(normalRole);
        userRepository.save(normalUser);
    }

    @Test
    @DisplayName(
            value =
                    """
            If the Authorization header is null,
            The JwtAuthenticationFilter should continue, and the API should return status 401
            """)
    void doFilterInternal_WhenAuthenticationHeaderMissing_ShouldReturnStatus401() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, null);

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
                this.restTemplate.exchange(
                        uri, HttpMethod.GET, requestEntity, ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName(
            value =
                    """
            If a token is reused after logout.
            any protected API (in this case, GET /v1/product API) should return status 401 (RevokedTokenException)
            """)
    void doFilterInternal_WhenTokenReusedAfterLogout_ShouldReturnStatus401() {
        var token = getToken();
        logout(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var requestEntity = new HttpEntity<>(headers);
        var page = 0;
        var size = 1;

        var endpoint = "/v1/products";
        var uri =
                UriComponentsBuilder.fromUriString(endpoint)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .encode()
                        .toUriString();

        var expectedStatus = HttpStatus.UNAUTHORIZED;

        var response =
                this.restTemplate.exchange(
                        uri, HttpMethod.GET, requestEntity, ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();

        Assertions.assertThat(body.getStatusCode()).isEqualTo(expectedStatus.value());
        Assertions.assertThat(body.getError()).isEqualTo(expectedStatus.getReasonPhrase());
        Assertions.assertThat(body.getExceptionName())
                .isEqualTo(RevokedTokenException.class.getSimpleName());
        Assertions.assertThat(body.getMessage()).isEqualTo(ErrorMessageConstants.BLACKLISTED_TOKEN);
        Assertions.assertThat(body.getPath()).isEqualTo(endpoint);
        Assertions.assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName(
            value =
                    """
            If the given JWT token is invalid,
            any protected API (in this case, GET /v1/product API) should return status 401 (JwtException)
            """)
    void doFilterInternal_WhenJwtTokenInvalid_ShouldReturnStatus401() {
        HttpHeaders headers = new HttpHeaders();
        var token = GeneratorUtils.generateUUID();
        headers.setBearerAuth(token);

        var requestEntity = new HttpEntity<>(headers);
        var page = 0;
        var size = 1;

        var endpoint = "/v1/products";
        var uri =
                UriComponentsBuilder.fromUriString(endpoint)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .encode()
                        .toUriString();

        var expectedStatus = HttpStatus.UNAUTHORIZED;

        var response =
                this.restTemplate.exchange(
                        uri, HttpMethod.GET, requestEntity, ApiErrorResponse.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        Assertions.assertThat(response.getBody()).isNotNull();

        var body = response.getBody();

        Assertions.assertThat(body.getStatusCode()).isEqualTo(expectedStatus.value());
        Assertions.assertThat(body.getError()).isEqualTo(expectedStatus.getReasonPhrase());
        Assertions.assertThat(body.getExceptionName())
                .isEqualTo(MalformedJwtException.class.getSimpleName());
        Assertions.assertThat(body.getMessage()).isNotEmpty();
        Assertions.assertThat(body.getPath()).isEqualTo(endpoint);
        Assertions.assertThat(body.getTimestamp()).isNotNull();
    }

    private String getToken() {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername(NORMAL_USERNAME);
        loginRequest.setPassword(NORMAL_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse =
                this.restTemplate.postForEntity("/auth/login", loginRequest, LoginResponse.class);
        Assertions.assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(loginResponse.getBody()).isNotNull();

        return loginResponse.getBody().token();
    }

    private void logout(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var logoutRequest = new HttpEntity<>(headers);
        ResponseEntity<String> logoutResponse =
                this.restTemplate.postForEntity("/auth/logout", logoutRequest, String.class);
        Assertions.assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
