package com.design.order_management_system.security;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.model.security.RevokedToken;
import com.design.order_management_system.repository.RevokedTokenRepository;
import com.design.order_management_system.utils.HashUtils;
import com.design.order_management_system.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.Instant;

@Slf4j
@Component
public class JwtLogoutHandler implements LogoutHandler {
    private final RevokedTokenRepository revokedTokenRepository;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtLogoutHandler(
            @Qualifier(value = "handlerExceptionResolver") HandlerExceptionResolver resolver,
            RevokedTokenRepository revokedTokenRepository
    ) {
        this.exceptionResolver = resolver;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    @NullMarked
    public void logout(HttpServletRequest request, HttpServletResponse response, @Nullable Authentication authentication) {
        var principalUser = SecurityUtils.getPrincipalUserFromAuthentication(authentication);
        var userId = principalUser.getUserId();

        log.info("Logout attempt; userId={}", userId);

        String authHeader = request.getHeader(CommonConstants.AUTHORIZATION_HEADER_KEY);

        if (authHeader == null || !authHeader.startsWith(CommonConstants.BEARER_TOKEN_PREFIX)) {
            log.info("Logout attempt failed; userId={} reason=token_missing", userId);
            exceptionResolver.resolveException(request, response, null, new InsufficientAuthenticationException(ErrorMessageConstants.BEARER_TOKEN_MISSING));
            return;
        }

        var token = authHeader.substring(CommonConstants.BEARER_TOKEN_PREFIX.length());
        var tokenHash = HashUtils.sha256(token);

        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
            log.info("Logout completed; userId={} reason=token_already_revoked", userId);
            return;
        }

        var revokedToken = RevokedToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(principalUser.getExpiryTime())
                .revokedAt(Instant.now())
                .build();

        var savedRevokedToken = revokedTokenRepository.save(revokedToken);

        log.info("Token revoked; userId={} revokedTokenId={}", userId, savedRevokedToken.getId());
    }
}
