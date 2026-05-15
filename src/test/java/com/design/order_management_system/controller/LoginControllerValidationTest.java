package com.design.order_management_system.controller;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.service.LoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
class LoginControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    @Test
    @DisplayName(value = """
            POST /auth/login with a missing (null) username
            should respond with HTTP status 400
            Note: token validity is tested in LoginServiceTest
            """)
    void login_WhenUsernameMissing_ShouldReturnStatus400() throws Exception {
        var request = new LoginRequest();
        request.setPassword("P0");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loginService);
    }

    @Test
    @DisplayName(value = """
            POST /auth/login with a blank password
            should respond with HTTP status 400
            Note: token validity is tested in LoginServiceTest
            """)
    void login_WhenBlankPassword_ShouldReturnStatus400() throws Exception {
        var request = new LoginRequest();
        request.setUsername("U0");
        request.setPassword("");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loginService);
    }
}