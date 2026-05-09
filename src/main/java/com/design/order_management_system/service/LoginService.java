package com.design.order_management_system.service;

import com.design.order_management_system.dto.request.LoginRequest;
import com.design.order_management_system.dto.response.LoginResponse;
import com.design.order_management_system.dto.security.PrincipalUser;
import com.design.order_management_system.exception.InvalidCredentialsException;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse getToken(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
        boolean match = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if (!match) {
            throw InvalidCredentialsException.forUsername(username);
        }

        return new LoginResponse(SecurityUtils.generateJwtToken(new PrincipalUser(user)));
    }
}
