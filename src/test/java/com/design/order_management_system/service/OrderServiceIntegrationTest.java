package com.design.order_management_system.service;

import com.design.order_management_system.config.DatabaseTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.domain.Order;
import com.design.order_management_system.model.enumeration.OrderStatus;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.OrderRepository;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.test_utils.GeneratorUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceIntegrationTest extends DatabaseTest {
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private OrderRepository orderRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private OrderService orderService;

  @Test
  @DisplayName(
      value =
          """
            If a draft order for the given user ID exists,
            The getDraftOrder method should return it.
            """)
  void getDraftOrder_WhenDraftOrderExists_ShouldReturnExisingDraftOrder() {
    var owner =
        transactionTemplate.execute(
            _ -> {
              var role = roleRepository.findByName(CommonConstants.ROLE_USER).orElseThrow();
              var user =
                  User.builder()
                      .username(GeneratorUtils.generateUUID())
                      .password("lkjwer98790xcv")
                      .build();
              user.addRole(role);
              return userRepository.save(user);
            });

    var orderId =
        transactionTemplate.execute(
            _ -> {
              var order = Order.builder().orderStatus(OrderStatus.CREATED).user(owner).build();

              return orderRepository.save(order).getId();
            });

    transactionTemplate.executeWithoutResult(
        _ -> {
          var draftOrder = orderService.getDraftOrder(owner.getId());
          Assertions.assertThat(draftOrder.getId()).isEqualTo(orderId);
        });
  }

  @Test
  @DisplayName(
      value =
          """
            If a draft order for the given user ID does not exist,
            The getDraftOrder method should return a new Draft Order.
            """)
  void getDraftOrder_WhenDraftOrderDoesNotExist_ShouldReturnNewDraftOrder() {
    var owner =
        transactionTemplate.execute(
            _ -> {
              var role = roleRepository.findByName(CommonConstants.ROLE_USER).orElseThrow();
              var user =
                  User.builder()
                      .username(GeneratorUtils.generateUUID())
                      .password("lkjwer98790xcv")
                      .build();
              user.addRole(role);
              return userRepository.save(user);
            });

    transactionTemplate.executeWithoutResult(
        _ -> {
          Assertions.assertThat(orderRepository.count()).isZero();

          var newDraftOrder = orderService.getDraftOrder(owner.getId());
          Assertions.assertThat(newDraftOrder.getId()).isNotNull();
          Assertions.assertThat(newDraftOrder.getCreatedAt()).isNotNull();
          Assertions.assertThat(newDraftOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
          Assertions.assertThat(newDraftOrder.getUser().getId()).isEqualTo(owner.getId());
          Assertions.assertThat(newDraftOrder.getOrderItems()).isEmpty();

          var persistedOrder = orderRepository.findById(newDraftOrder.getId()).orElseThrow();
          Assertions.assertThat(persistedOrder.getId()).isNotNull();
          Assertions.assertThat(persistedOrder.getCreatedAt()).isNotNull();
          Assertions.assertThat(persistedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
          Assertions.assertThat(persistedOrder.getUser().getId()).isEqualTo(owner.getId());
          Assertions.assertThat(persistedOrder.getOrderItems()).isEmpty();
        });
  }
}
