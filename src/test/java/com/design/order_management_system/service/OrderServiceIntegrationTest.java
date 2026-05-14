package com.design.order_management_system.service;

import com.design.order_management_system.config.seeder.DataSeeder;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.request.OrderRequest;
import com.design.order_management_system.exception.InsufficientResourcesException;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.domain.OrderItem;
import com.design.order_management_system.model.domain.Product;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.ProductRepository;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.utils.TestSecurityUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;

@AutoConfigureTestEntityManager
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderServiceIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private OrderRepository orderRepository;

    private long userId;
    private long productId0;
    private long productId1;
    private long stock0;
    private long stock1;
    private BigDecimal price0;
    private BigDecimal price1;

    private static final String USERNAME = "B";
    private static final String PASSWORD = "NRL@5896";

    @BeforeEach
    void setup() {
        var normalRole = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(DataSeeder.ROLE_USER_WAS_NOT_SEEDED));
        var normalUser = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .build();
        normalUser.addRole(normalRole);
        var savedUser = userRepository.save(normalUser);
        this.userId = savedUser.getId();

        TestSecurityUtils.authenticateUser(userId, USERNAME, CommonConstants.ROLE_USER);

        String productName0 = "Pr0";
        String productName1 = "Pr1";
        stock0 = 4L;
        stock1 = 3L;
        price0 = BigDecimal.valueOf(20.0);
        price1 = BigDecimal.valueOf(30.0);

        var savedProduct0 = productRepository.save(Product.builder()
                .name(productName0)
                .price(price0)
                .stock(stock0)
                .build());

        var savedProduct1 = productRepository.save(Product.builder()
                .name(productName1)
                .price(price1)
                .stock(stock1)
                .build());

        productId0 = savedProduct0.getId();
        productId1 = savedProduct1.getId();
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearAuthenticationContext();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName(value = """
            If one of the requested products has insufficient stock,
            the registerOrder method should throw a InsufficientResourcesException and
            product stocks should not be modified.
            """)
    void registerOrder_WhenInsufficientStock_ShouldNotUpdateProductStock() {
        Long quantity = 4L;

        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(productId0)
                .quantity(quantity)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(productId1)
                .quantity(quantity)
                .build();
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        Assertions.assertThatThrownBy(() -> orderService.registerOrder(createOrderRequest))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessage(String.format(InsufficientResourcesException.INSUFFICIENT_RESOURCES,
                        CommonConstants.PRODUCT,
                        CommonConstants.STOCK,
                        quantity,
                        stock1)
                );

        transactionTemplate.executeWithoutResult(_ -> {
            var committedProduct0 = entityManager.find(Product.class, productId0);
            var committedProduct1 = entityManager.find(Product.class, productId1);
            Assertions.assertThat(committedProduct0).isNotNull();
            Assertions.assertThat(committedProduct1).isNotNull();
            Assertions.assertThat(committedProduct0.getStock()).isEqualTo(stock0);
            Assertions.assertThat(committedProduct1.getStock()).isEqualTo(stock1);
        });
    }

    @Test
    @DisplayName(value = """
            If all the requested products have a sufficient stock,
            the registerOrder method transaction should persist order and
            product stocks should be updated.
            """)
    void registerOrder_WhenSufficientStock_ShouldPersistOrderAndUpdateProductStock() {
        long quantity = 3L;

        BigDecimal product0PurchasePrice = BigDecimal.valueOf(quantity).multiply(price0);
        BigDecimal product1PurchasePrice = BigDecimal.valueOf(quantity).multiply(price1);
        BigDecimal totalPrice = product0PurchasePrice.add(product1PurchasePrice);
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(productId0)
                .quantity(quantity)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(productId1)
                .quantity(quantity)
                .build();
        var createOrderRequest = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        var response = orderService.registerOrder(createOrderRequest);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getOrderId()).isNotNull();
        Assertions.assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(response.getTotalPrice()).isEqualByComparingTo(totalPrice);

        transactionTemplate.executeWithoutResult(_ -> {
            var committedOrder = entityManager.find(Order.class, response.getOrderId());
            Assertions.assertThat(committedOrder).isNotNull();
            Assertions.assertThat(committedOrder.getUser()).isNotNull();
            Assertions.assertThat(committedOrder.getUser().getId()).isEqualTo(userId);
            Assertions.assertThat(committedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
            Assertions.assertThat(committedOrder.getOrderItems())
                    .hasSize(2)
                    .extracting(item -> item.getProduct().getId(), OrderItem::getQuantity)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactlyInAnyOrder(tuple(productId0, quantity), tuple(productId1, quantity));
            Assertions.assertThat(committedOrder.getOrderItems())
                    .extracting(OrderItem::getPurchasePrice)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactlyInAnyOrder(price0, price1);
            // containsExactlyInAnyOrder does not use isEqualByComparison, so assert the purchase prices separately.
            Assertions.assertThat(committedOrder.getOrderItems().getFirst().getPurchasePrice()).isEqualByComparingTo(price0);
            Assertions.assertThat(committedOrder.getOrderItems().get(1).getPurchasePrice()).isEqualByComparingTo(price1);
            var committedProduct0 = entityManager.find(Product.class, productId0);
            var committedProduct1 = entityManager.find(Product.class, productId1);
            Assertions.assertThat(committedProduct0).isNotNull();
            Assertions.assertThat(committedProduct1).isNotNull();
            Assertions.assertThat(committedProduct0.getStock()).isEqualTo(stock0 - quantity);
            Assertions.assertThat(committedProduct1.getStock()).isEqualTo(stock1 - quantity);
        });
    }
}
