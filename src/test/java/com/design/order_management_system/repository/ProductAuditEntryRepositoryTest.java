package com.design.order_management_system.repository;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.domain.ProductAuditEntry;
import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.model.security.User;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;

@DataJpaTest
class ProductAuditEntryRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ProductAuditEntryRepository repository;

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    @Test
    @DisplayName(value = """
            When an audit entry for the given product ID and version exist,
            The repository should return the corresponding entry
            with user entity details loaded.
            """)
    void findByProduct_IdAndVersion_WhenEntryExists_ShouldReturnProductAuditEntryWithLoadedUser() {
        var role = entityManager.persist(Role.builder()
                .name(CommonConstants.ROLE_USER)
                .build());

        var userName = "U0";
        var unsavedUser = User.builder()
                .username(userName)
                .password("P0")
                .build();
        unsavedUser.addRole(role);
        var user = entityManager.persist(unsavedUser);

        var productName = "Pr0";
        var productPrice = BigDecimal.TEN;
        var productStock = 10L;
        var product = entityManager.persist(Product.builder()
                .name(productName)
                .price(productPrice)
                .stock(productStock)
                .build());
        var productId = product.getId();

        var version = 3L;
        var savedAuditEntry = entityManager.persist(ProductAuditEntry.builder()
                .version(version)
                .productName(productName)
                .price(productPrice)
                .stock(productStock)
                .operationType(OperationType.CREATE)
                .user(user)
                .product(product)
                .build());

        entityManager.flush();
        entityManager.clear();

        var fetchedAuditEntry = repository.findByProduct_IdAndVersion(productId, version);
        Assertions.assertThat(fetchedAuditEntry).isPresent();

        var auditEntry = fetchedAuditEntry.get();

        Assertions.assertThat(auditEntry.getId()).isEqualTo(savedAuditEntry.getId());
        Assertions.assertThat(auditEntry.getVersion()).isEqualTo(version);
        Assertions.assertThat(auditEntry.getProductName()).isEqualTo(productName);
        Assertions.assertThat(auditEntry.getPrice()).isEqualByComparingTo(productPrice);
        Assertions.assertThat(auditEntry.getStock()).isEqualTo(productStock);
        Assertions.assertThat(auditEntry.getOperationType()).isEqualTo(OperationType.CREATE);
        Assertions.assertThat(auditEntry.getCreatedAt()).isNotNull();
        Assertions.assertThat(PERSISTENCE_UTIL.isLoaded(auditEntry, "user")).isTrue();
        Assertions.assertThat(auditEntry.getUser().getUsername()).isEqualTo(userName);
    }
}
