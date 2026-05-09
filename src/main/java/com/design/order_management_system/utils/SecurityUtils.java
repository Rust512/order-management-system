package com.design.order_management_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
    private static final long TOKEN_EXPIRY_IN_MINUTES = 10L;
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    public static String generateJwtToken(UserDetails user) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        var claims = Map.of(ROLES, roles);
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
}
