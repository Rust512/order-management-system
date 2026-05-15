package com.design.order_management_system.security;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.security.PrincipalUser;
import com.design.order_management_system.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(@Qualifier(value = "handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.exceptionResolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(CommonConstants.AUTHORIZATION_HEADER_KEY);

        if (authHeader == null || !authHeader.startsWith(CommonConstants.BEARER_TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(CommonConstants.BEARER_TOKEN_PREFIX.length());

        try {
            Claims claims = SecurityUtils.parseJwtToken(token);
            PrincipalUser user = new PrincipalUser(
                    claims.get(SecurityUtils.USER_ID, Long.class),
                    claims.getSubject(),
                    extractRoles(claims)
            );
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("JWT auth failed; path={} reason=invalid_token", request.getRequestURI());
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    private List<String> extractRoles(Claims claims) {
        List<?> rawList = claims.get(SecurityUtils.ROLES, List.class);
        return rawList.stream()
                .map(Object::toString)
                .toList();
    }
}
