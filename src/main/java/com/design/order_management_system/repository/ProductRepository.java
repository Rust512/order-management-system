package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            SELECT CASE WHEN (COUNT(product) > 0) THEN TRUE ELSE FALSE END
            FROM Product product
            WHERE product.name = :name
            """)
    boolean existsByName(@Param("name") String name);
}
