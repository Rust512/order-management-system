package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            SELECT CASE WHEN (COUNT(product) > 0) THEN TRUE ELSE FALSE END
            FROM Product product
            WHERE product.name = :name
            """)
    boolean existsByName(@Param("name") String name);

    @Modifying
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT product
            FROM Product product
            WHERE product.id = :productId
            """)
    Optional<Product> findByIdForUpdate(Long productId);
}
