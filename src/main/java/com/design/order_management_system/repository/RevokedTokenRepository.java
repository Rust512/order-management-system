package com.design.order_management_system.repository;

import com.design.order_management_system.model.security.RevokedToken;
import org.springframework.data.repository.CrudRepository;

public interface RevokedTokenRepository extends CrudRepository<RevokedToken, Long> {
}
