package com.design.order_management_system.repository;

import com.design.order_management_system.model.security.RevokedToken;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends CrudRepository<RevokedToken, Long> {
    boolean existsByTokenHashAndExpiresAtAfter(String token, Instant expiresAt);
    boolean existsByTokenHash(String tokenHash);
}
