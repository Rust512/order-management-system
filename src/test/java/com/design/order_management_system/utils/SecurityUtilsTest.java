package com.design.order_management_system.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.design.order_management_system.model.security.PrincipalUser;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

  @Test
  @DisplayName(
      value =
          """
            If the list of roles in the given principal user object is empty,
            the isAdmin method should return false.
            """)
  void isAdmin() {
    var user = mock(PrincipalUser.class);
    when(user.getRoles()).thenReturn(List.of());
    Assertions.assertThat(SecurityUtils.isAdmin(user)).isFalse();
  }

  @Test
  @DisplayName(
      """
            If the provided authentication object is null,
            the getPrincipalUserFromAuthentication method should throw AuthorizationServiceException
            """)
  void
      getPrincipalUserFromAuthentication_WhenAuthenticationNull_ShouldThrowAuthorizationServiceException() {
    Assertions.assertThatThrownBy(() -> SecurityUtils.getPrincipalUserFromAuthentication(null))
        .isInstanceOf(AuthorizationServiceException.class)
        .hasMessage("Security context empty");
  }

  @Test
  @DisplayName(
      """
            If the principal in the provided authentication object is null,
            the getPrincipalUserFromAuthentication method should throw AuthorizationServiceException
            """)
  void
      getPrincipalUserFromAuthentication_WhenAuthenticationPrincipalNull_ShouldThrowAuthorizationServiceException() {
    var authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(null);
    Assertions.assertThatThrownBy(
            () -> SecurityUtils.getPrincipalUserFromAuthentication(authentication))
        .isInstanceOf(AuthorizationServiceException.class)
        .hasMessage("Missing principal user");
  }

  @Test
  @DisplayName(
      """
            If the principal in the provided authentication object is not an instance of the PrincipalUser class,
            the getPrincipalUserFromAuthentication method should throw AuthorizationServiceException
            """)
  void
      getPrincipalUserFromAuthentication_WhenAuthenticationPrincipalNotPrincipalUserInstance_ShouldThrowAuthorizationServiceException() {
    var authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(new Object());
    Assertions.assertThatThrownBy(
            () -> SecurityUtils.getPrincipalUserFromAuthentication(authentication))
        .isInstanceOf(AuthorizationServiceException.class)
        .hasMessage("Invalid Authentication Principal");
  }
}
