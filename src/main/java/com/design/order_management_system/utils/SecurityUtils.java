package com.design.order_management_system.utils;

import com.design.order_management_system.dto.security.PrincipalUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static final String ROLES = "roles";
    public static final String USER_ID = "userId";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String SECURITY_CONTEXT_EMPTY = "Security context empty";
    public static final String PRINCIPAL_IS_NULL = "Principal is null!";
    private static final long TOKEN_EXPIRY_IN_MINUTES = 10L;
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

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
}
