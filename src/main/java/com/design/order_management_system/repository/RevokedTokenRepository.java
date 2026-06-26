package com.design.order_management_system.repository;

import com.design.order_management_system.model.security.RevokedToken;
import java.time.Instant;
import org.springframework.data.repository.CrudRepository;

public interface RevokedTokenRepository extends CrudRepository<RevokedToken, Long> {
  boolean existsByTokenHashAndExpiresAtAfter(String token, Instant expiresAt);

  boolean existsByTokenHash(String tokenHash);
}
