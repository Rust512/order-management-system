package com.design.order_management_system.repository;

import com.design.order_management_system.model.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query(value = """
            SELECT role
            FROM Role role
            WHERE role.name = :name
            """)
    Optional<Role> findByName(@Param("name") String name);

    @Query(value = """
            SELECT CASE WHEN (COUNT(role) > 0) THEN TRUE ELSE FALSE END
            FROM Role role
            WHERE role.name = :name
            """)
    boolean existsByName(@Param("name") String name);
}
