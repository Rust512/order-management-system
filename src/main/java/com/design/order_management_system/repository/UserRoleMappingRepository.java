package com.design.order_management_system.repository;

import com.design.order_management_system.model.UserRoleMapping;
import com.design.order_management_system.model.UserRoleMappingKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, UserRoleMappingKey> {
}
