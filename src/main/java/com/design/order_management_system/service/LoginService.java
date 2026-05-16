package com.design.order_management_system.service;

import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.exception.InvalidCredentialsException;
import com.design.order_management_system.model.security.PrincipalUser;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse getToken(LoginRequest loginRequest) {
        log.info("Login attempt by username={}.", loginRequest.getUsername());
        String username = loginRequest.getUsername();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed for username={} reason=user_not_found", username);
                    return InvalidCredentialsException.forUsername(username);
                });
        boolean match = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if (!match) {
            log.warn("Login failed for username={} reason=invalid_password", username);
            throw InvalidCredentialsException.forUsername(username);
        }

        log.info("Login successful for userId={}, username={}.", user.getId(), user.getUsername());
        return new LoginResponse(SecurityUtils.generateJwtToken(new PrincipalUser(user)));
    }
}
