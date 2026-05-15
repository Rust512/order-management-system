package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.exception.InvalidCredentialsException;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    private static final String USER_NOT_FOUND = "user not found";

    @Test
    @DisplayName(value = """
            When the given username does not exist,
            the method should throw a UsernameNotFoundException
            """)
    void getToken_WhenUsernameDoesNotExist_ShouldThrowException() {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("U0");

        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> loginService.getToken(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining(loginRequest.getUsername());

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName(value = """
            When the given username exists, and the given password is incorrect,
            the method should throw a InvalidCredentialsException
            """)
    void getToken_WhenUsernameExistsAndPasswordMismatch_ShouldThrowException() {
        String username = "U0";
        String password = "P0";
        String hashedPassword = "HP0";

        var loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        var user = User.builder()
                .username(username)
                .password(hashedPassword)
                .build();

        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), hashedPassword)).thenReturn(false);

        Assertions.assertThatThrownBy(() -> loginService.getToken(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining(username);

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), hashedPassword);
    }

    @Test
    @DisplayName(value = """
            When the given username exists, and the password is correct,
            the method should return a login respond with the correct JWT token
            """)
    void getToken_WhenUsernameExistsAndPasswordCorrect_ShouldReturnCorrectJwtToken() {
        String username = "U0";
        String password = "P0";
        String hashedPassword = "HP0";

        var loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        var user = User.builder()
                .id(1L)
                .username(username)
                .password(hashedPassword)
                .build();

        var role = Role.builder()
                .id(1L)
                .name(CommonConstants.ROLE_USER)
                .build();

        user.addRole(role);

        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

        var response = loginService.getToken(loginRequest);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.token()).isNotBlank();

        Claims claims = SecurityUtils.parseJwtToken(response.token());
        Long userId = claims.get(SecurityUtils.USER_ID, Long.class);
        Assertions.assertThat(userId).isEqualTo(1L);

        String subject = claims.getSubject();
        Assertions.assertThat(subject).isEqualTo(username);

        List<?> rawList = claims.get(SecurityUtils.ROLES, List.class);
        List<String> roles = rawList.stream()
                .map(Objects::toString)
                .toList();
        Assertions.assertThat(roles).containsExactly(CommonConstants.ROLE_USER);

        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), hashedPassword);
    }
}