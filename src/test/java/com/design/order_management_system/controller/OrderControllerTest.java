package com.design.order_management_system.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.design.order_management_system.annotation.WebMvcSliceTest;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.constants.ErrorMessageConstants;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.exception.ResourceNotOwnedException;
import com.design.order_management_system.service.OrderAuditEntryService;
import com.design.order_management_system.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcSliceTest(OrderController.class)
class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OrderAuditEntryService orderAuditEntryService;

  @MockitoBean private OrderService orderService;

  @Test
  @DisplayName(
      value =
          """
            If the OrderService::getOrderById method throws a ResourceNotFoundException,
            the GET /v1/orders/{id} API should respond with HTTP status code 404
            """)
  void getOrder_WhenUserAdminAndOrderMissing_ShouldReturnStatusCode404() throws Exception {
    var orderId = 1L;

    var expectedStatus = HttpStatus.NOT_FOUND;
    var expectedMessage =
        String.format(
            ErrorMessageConstants.RESOURCE_NOT_FOUND, CommonConstants.ORDER, "id", orderId);

    when(orderService.getOrderById(orderId))
        .thenThrow(
            new ResourceNotFoundException(CommonConstants.ORDER, "id", String.valueOf(orderId)));

    mockMvc
        .perform(get("/v1/orders/{id}", orderId))
        .andExpect(status().is(expectedStatus.value()))
        .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
        .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
        .andExpect(jsonPath("$.sMessage").value(expectedMessage))
        .andExpect(jsonPath("$.sPath").value(String.format("/v1/orders/%d", orderId)))
        .andExpect(
            jsonPath("$.sExceptionName").value(ResourceNotFoundException.class.getSimpleName()));

    verify(orderService).getOrderById(orderId);
    verifyNoMoreInteractions(orderService);
  }

  @Test
  @DisplayName(
      value =
          """
            If the OrderService::getOrderById method throws a ResourceNotOwnedException,
            the GET /v1/orders/{id} API should respond with HTTP status code 403
            """)
  void getOrder_WhenUserNotAdminAndOrderMissing_ShouldReturnStatusCode403() throws Exception {
    var orderId = 1L;
    var userId = 2L;

    var expectedStatus = HttpStatus.FORBIDDEN;
    var expectedMessage =
        String.format(
            ErrorMessageConstants.RESOURCE_NOT_OWNED, CommonConstants.ORDER, "id", orderId, userId);

    when(orderService.getOrderById(orderId))
        .thenThrow(
            new ResourceNotOwnedException(
                CommonConstants.ORDER, "id", String.valueOf(orderId), userId));

    mockMvc
        .perform(get("/v1/orders/{id}", orderId))
        .andExpect(status().is(expectedStatus.value()))
        .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
        .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
        .andExpect(jsonPath("$.sMessage").value(expectedMessage))
        .andExpect(jsonPath("$.sPath").value(String.format("/v1/orders/%d", orderId)))
        .andExpect(
            jsonPath("$.sExceptionName").value(ResourceNotOwnedException.class.getSimpleName()));

    verify(orderService).getOrderById(orderId);
    verifyNoMoreInteractions(orderService);
  }
}
