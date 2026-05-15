package com.design.order_management_system.utils;

import com.design.order_management_system.model.security.PrincipalUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class SecurityUtils {

    private SecurityUtils() {
    }

    public static final String ROLES = "roles";
    public static final String USER_ID = "userId";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final long TOKEN_EXPIRY_IN_MINUTES = 10L;
    private static final String KEY = "RtpMd6zuyJMxz4YhZjJ2CwAKuwGzH5kQ";
    private static final String MISSING_PRINCIPAL = "Missing principal user";
    private static final String SECURITY_CONTEXT_EMPTY = "Security context empty";
    private static final String INVALID_AUTHENTICATION_PRINCIPAL = "Invalid Authentication Principal";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8));

    public static String generateJwtToken(PrincipalUser user) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        var claims = Map.of(
                ROLES, roles,
                USER_ID, user.getUserId()
        );
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .expiration(Date.from(Instant.now()
                        .plus(TOKEN_EXPIRY_IN_MINUTES, ChronoUnit.MINUTES)))
                .issuedAt(Date.from(Instant.now()))
                .signWith(SECRET_KEY, StandardSecureDigestAlgorithms.findBySigningKey(SECRET_KEY))
                .compact();
    }

    public static Claims parseJwtToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean isAdmin(PrincipalUser principalUser) {
        List<String> roles = principalUser.getRoles();
        return !roles.isEmpty() && roles.contains(ROLE_ADMIN);
    }

    public static PrincipalUser getPrincipalUser() {
        var token = SecurityContextHolder.getContext()
                .getAuthentication();
        if (token == null) {
            log.error("Security context invalid; reason=authentication_missing");
            throw new AuthorizationServiceException(SECURITY_CONTEXT_EMPTY);
        }

        var principal = token.getPrincipal();

        if (principal == null) {
            log.error("Security context invalid; reason=principal_missing");
            throw new AuthorizationServiceException(MISSING_PRINCIPAL);
        }

        if (!(principal instanceof PrincipalUser principalUser)) {
            String principalType = principal.getClass().getSimpleName();
            log.error("Security context invalid; principalType={} reason=invalid_principal", principalType);
            throw new AuthorizationServiceException(INVALID_AUTHENTICATION_PRINCIPAL);
        }

        return principalUser;
    }
}
