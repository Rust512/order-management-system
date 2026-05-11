package com.design.order_management_system.repository;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
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
class OrderRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    @Test
    @DisplayName("""
            The getOrderByIdAndUserIdWithItems method, when the order corresponding to the
            given order ID is not owned by the current user, it should return an empty Optional object.
            """)
    void getOrderByIdAndUserIdWithItems_WhenUserDoesNotOwnOrder_ShouldReturnEmpty() {
        var product = entityManager.persist(Product.builder()
                .name("Pr0")
                .price(BigDecimal.valueOf(1.0))
                .stock(1L)
                .build()
        );

        var role = entityManager.persist(Role.builder().name(CommonConstants.ROLE_USER).build());
        var user0 = User.builder().username("U0").password("P0").build();
        user0.addRole(role);
        var savedUser0 = entityManager.persist(user0);
        var user1 = User.builder().username("U1").password("P1").build();
        user1.addRole(role);
        var savedUser1 = entityManager.persist(user1);

        Long user0Id = savedUser0.getId();

        var orderItem = OrderItem.builder()
                .product(product)
                .quantity(1L)
                .purchasePrice(BigDecimal.valueOf(1.0))
                .build();

        var order = Order.builder()
                .orderStatus(OrderStatus.CREATED)
                .user(savedUser1)
                .build();

        order.addOrderItem(orderItem);
        var savedOrder = entityManager.persist(order);
        Long savedOrderId = savedOrder.getId();
        entityManager.flush();
        entityManager.clear();
        var fetchedOrder = orderRepository.getOrderByIdAndUserIdWithItems(savedOrderId, user0Id);
        Assertions.assertThat(fetchedOrder).isNotPresent();
    }

    @Test
    @DisplayName("""
            The getOrderByIdAndUserIdWithItems method, when the order corresponding to the
            given order ID does not exist, it should return an empty Optional object.
            """)
    void getOrderByIdAndUserIdWithItems_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        var role = entityManager.persist(Role.builder().name(CommonConstants.ROLE_USER).build());
        var user0 = User.builder().username("U0").password("P0").build();
        user0.addRole(role);
        var savedUser0 = entityManager.persist(user0);

        Long user0Id = savedUser0.getId();
        var order = Order.builder()
                .orderStatus(OrderStatus.CREATED)
                .user(savedUser0)
                .build();

        var savedOrder = entityManager.persist(order);
        entityManager.flush();
        entityManager.clear();
        Long nonExistentOrderId = savedOrder.getId() + 1000L;
        var fetchedOrder = orderRepository.getOrderByIdAndUserIdWithItems(nonExistentOrderId, user0Id);
        Assertions.assertThat(fetchedOrder).isNotPresent();
    }

    @Test
    @DisplayName("""
            The getOrderByIdAndUserIdWithItems method, when the order corresponding to the
            given order ID is owned by the current user, it should return the corresponding order.
            """)
    void getOrderByIdAndUserIdWithItems_WhenUserOwnsOrder_ShouldReturnOrder() {
        var product = entityManager.persist(Product.builder()
                .name("Pr0")
                .price(BigDecimal.valueOf(1.0))
                .stock(1L)
                .build()
        );

        var role = entityManager.persist(Role.builder().name(CommonConstants.ROLE_USER).build());
        var user0 = User.builder().username("U0").password("P0").build();
        user0.addRole(role);
        var savedUser0 = entityManager.persist(user0);

        Long user0Id = savedUser0.getId();

        var orderItem = OrderItem.builder()
                .product(product)
                .quantity(1L)
                .purchasePrice(BigDecimal.valueOf(1.0))
                .build();

        var order = Order.builder()
                .orderStatus(OrderStatus.CREATED)
                .user(savedUser0)
                .build();

        order.addOrderItem(orderItem);
        var savedOrder = entityManager.persist(order);
        Long savedOrderId = savedOrder.getId();
        entityManager.flush();
        entityManager.clear();

        var fetchedOrder = orderRepository.getOrderByIdAndUserIdWithItems(savedOrderId, user0Id);
        Assertions.assertThat(fetchedOrder).isPresent();

        var orderObj = fetchedOrder.get();
        Assertions.assertThat(PERSISTENCE_UTIL.isLoaded(orderObj, "orderItems")).isTrue();
        Assertions.assertThat(orderObj.getOrderItems())
                .hasSize(1)
                .allSatisfy(item -> Assertions.assertThat(PERSISTENCE_UTIL.isLoaded(item, "product"))
                        .isTrue());
        Assertions.assertThat(orderObj.getId()).isEqualTo(savedOrderId);
        Assertions.assertThat(orderObj.getUser().getId()).isEqualTo(user0Id);
    }
}