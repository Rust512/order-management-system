package com.design.order_management_system.filter;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.exception.MissingTokenException;
import com.design.order_management_system.repository.RevokedTokenRepository;
import com.design.order_management_system.security.JwtLogoutHandler;
import com.design.order_management_system.utils.GeneratorUtils;
import com.design.order_management_system.utils.TestSecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtLogoutHandlerTest {
    @Mock
    private RevokedTokenRepository revokedTokenRepository;
    @Mock
    private HandlerExceptionResolver exceptionResolver;

    @InjectMocks
    private JwtLogoutHandler jwtLogoutHandler;

    @Test
    @DisplayName(value = """
            In the logout handler, if the authorization token is missing,
            the handler should resolve the MissingTokenException
            """)
    void logout_WhenTokenMissing_ShouldResolveMissingTokenException() {
        TestSecurityUtils.setAuthenticationContext(1L, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var result = mock(ModelAndView.class);

        when(request.getHeader(CommonConstants.AUTHORIZATION_HEADER_KEY)).thenReturn(null);
        when(exceptionResolver.resolveException(eq(request), eq(response), eq(null), any(MissingTokenException.class))).thenReturn(result);

        jwtLogoutHandler.logout(request, response, TestSecurityUtils.getAuthentication());

        var exceptionCaptor = ArgumentCaptor.forClass(MissingTokenException.class);
        verify(exceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());

        Assertions.assertThat(exceptionCaptor.getValue()).isNotNull();

        var exception = exceptionCaptor.getValue();

        Assertions.assertThat(exception.getMessage()).isEqualTo(ErrorMessageConstants.BEARER_TOKEN_MISSING);

        TestSecurityUtils.clearAuthenticationContext();
    }

    @Test
    @DisplayName(value = """
            In the logout handler, if the authorization token does not have the 'Bearer ' prefix,
            the handler should resolve the MissingTokenException
            """)
    void logout_WhenTokenDoesNotHaveBearerPrefix_ShouldThrowMissingTokenException() {
        TestSecurityUtils.setAuthenticationContext(1L, GeneratorUtils.generateUUID(), CommonConstants.ROLE_USER);

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var result = mock(ModelAndView.class);

        when(request.getHeader(CommonConstants.AUTHORIZATION_HEADER_KEY)).thenReturn(GeneratorUtils.generateUUID());
        when(exceptionResolver.resolveException(eq(request), eq(response), eq(null), any(MissingTokenException.class))).thenReturn(result);

        jwtLogoutHandler.logout(request, response, TestSecurityUtils.getAuthentication());

        var exceptionCaptor = ArgumentCaptor.forClass(MissingTokenException.class);
        verify(exceptionResolver).resolveException(eq(request), eq(response), eq(null), exceptionCaptor.capture());

        Assertions.assertThat(exceptionCaptor.getValue()).isNotNull();

        var exception = exceptionCaptor.getValue();

        Assertions.assertThat(exception.getMessage()).isEqualTo(ErrorMessageConstants.BEARER_TOKEN_MISSING);

        TestSecurityUtils.clearAuthenticationContext();
    }
}