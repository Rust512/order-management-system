package com.design.order_management_system.repository;

import com.design.order_management_system.model.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(
            value =
                    """
            SELECT CASE WHEN (COUNT(product) > 0) THEN TRUE ELSE FALSE END
            FROM Product product
            WHERE product.name = :name
            """)
    boolean existsByName(@Param("name") String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            value =
                    """
            SELECT product
            FROM Product product
            WHERE product.id = :productId
            """)
    Optional<Product> findByIdForUpdate(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            value =
                    """
            SELECT product
            FROM Product product
            WHERE product.id IN :productIds
            ORDER BY product.id ASC
            """)
    List<Product> findAllByIdInForWrite(List<Long> productIds);
}
