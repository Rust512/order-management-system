package com.design.order_management_system.repository;

import com.design.order_management_system.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = """
            SELECT user
            FROM User user
            WHERE user.username = :username
            """)
    Optional<User> findByUsername(@Param("username") String username);

    @Query(value = """
            SELECT CASE WHEN (COUNT(user) > 0) THEN TRUE ELSE FALSE END
            FROM User user
            WHERE user.username = :username
            """)
    boolean existsByUsername(@Param("username") String username);
}
