package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.repository.RevokedTokenRepository;
import com.design.order_management_system.utils.HashUtils;
import com.design.order_management_system.utils.SecurityUtils;
import com.design.order_management_system.utils.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevokedTokenRepository revokedTokenRepository;

    @BeforeEach
    void setUp() {
        TestSecurityUtils.setAuthenticationContext(1L, "U0", CommonConstants.ROLE_USER);
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            POST /auth/logout without a revoked auth token
            should logout, but no revoked token entry should be present in the DB.
            """)
    void logout_WithRevokedToken_ShouldReturnStatus401() throws Exception {
        var user = SecurityUtils.getPrincipalUser();
        var token = SecurityUtils.generateJwtToken(user);
        var hash = HashUtils.sha256(token);

        when(revokedTokenRepository.existsByTokenHashAndExpiresAtAfter(eq(hash), any())).thenReturn(false);
        when(revokedTokenRepository.existsByTokenHash(hash)).thenReturn(true);
        mockMvc.perform(post("/auth/logout")
                        .header(CommonConstants.AUTHORIZATION_HEADER_KEY, CommonConstants.BEARER_TOKEN_PREFIX + token))
                .andExpect(status().isOk());

        verify(revokedTokenRepository).existsByTokenHashAndExpiresAtAfter(eq(hash), any());
        verify(revokedTokenRepository).existsByTokenHash(hash);
        verifyNoMoreInteractions(revokedTokenRepository);
    }
}
