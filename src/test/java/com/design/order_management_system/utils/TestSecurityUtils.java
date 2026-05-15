package com.design.order_management_system.utils;

import com.design.order_management_system.model.security.PrincipalUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

public class TestSecurityUtils {
    private TestSecurityUtils() {
    }

    public static void setAuthenticationContext(long userId, String username, String... roles) {
        var principal = PrincipalUser.builder()
                .userId(userId)
                .username(username)
                .roles(Arrays.asList(roles))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static void clearAuthenticationContext() {
        SecurityContextHolder.clearContext();
    }
}
