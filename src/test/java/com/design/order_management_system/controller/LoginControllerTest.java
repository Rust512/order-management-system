package com.design.order_management_system.controller;

import com.design.order_management_system.annotation.WebMvcSliceTest;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.exception.InvalidCredentialsException;
import com.design.order_management_system.service.LoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcSliceTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    private static final String LOGIN_ENDPOINT = "/auth/login";

    @Test
    @DisplayName(value = """
            POST /auth/login with valid credentials should respond with
            HTTP status 200 OK, and a token.
            Note: token validity is tested in LoginServiceTest
            """)
    void login_WhenCredentialsAreValid_ShouldReturnToken() throws Exception {
        var request = new LoginRequest();
        request.setUsername("U0");
        request.setPassword("P0");
        String token = "Some token";
        var response = new LoginResponse(token);

        when(loginService.getToken(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sToken").value("Some token"));
    }

    @Test
    @DisplayName(value = "POST /auth/login with invalid credentials should respond with HTTP status 401")
    void login_WhenCredentialsAreInvalid_ShouldReturnStatus401() throws Exception {
        String username = "U0";
        var request = new LoginRequest();
        request.setUsername(username);
        request.setPassword("P0");

        var exception = InvalidCredentialsException.forUsername(username);

        when(loginService.getToken(any(LoginRequest.class))).thenThrow(exception);

        HttpStatus expectedStatus = HttpStatus.UNAUTHORIZED;

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(LOGIN_ENDPOINT))
                .andExpect(jsonPath("$.sMessage").value(exception.getMessage()))
                .andExpect(jsonPath("$.sExceptionName").value(InvalidCredentialsException.class.getSimpleName()));
    }
}