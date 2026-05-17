package com.design.order_management_system.controller;

import com.design.order_management_system.annotation.WebMvcSliceTest;
import com.design.order_management_system.dto.request.OrderItemRequest;
import com.design.order_management_system.dto.request.OrderRequest;
import com.design.order_management_system.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcSliceTest(OrderController.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private static final String ORDER_REGISTRATION_ENDPOINT = "/v1/orders";

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, if the orderItems array is missing,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenOrderItemsArrayNull_ShouldReturnStatus400() throws Exception {
        var request = OrderRequest.builder()
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, if the orderItems array is empty,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenOrderItemsArrayEmpty_ShouldReturnStatus400() throws Exception {
        var request = OrderRequest.builder()
                .orderItems(List.of())
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, any orderItem in the orderItems array
            has a missing productId,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenAnOrderItemHasMissingProductId_ShouldReturnStatus400() throws Exception {
        var orderItemRequest0 = OrderItemRequest.builder()
                .quantity(1L)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(1L)
                .quantity(1L)
                .build();
        var request = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, any orderItem in the orderItems array
            has zero productId,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenAnOrderItemHasZeroProductId_ShouldReturnStatus400() throws Exception {
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(0L)
                .quantity(1L)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(1L)
                .quantity(1L)
                .build();
        var request = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, any orderItem in the orderItems array
            has negative productId,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenAnOrderItemHasNegativeProductId_ShouldReturnStatus400() throws Exception {
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(-1L)
                .quantity(1L)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(1L)
                .quantity(1L)
                .build();
        var request = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, any orderItem in the orderItems array
            has a missing quantity value,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenAnOrderItemHasMissingQuantity_ShouldReturnStatus400() throws Exception {
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(1L)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(2L)
                .quantity(1L)
                .build();
        var request = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, any orderItem in the orderItems array
            has zero quantity value,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenAnOrderItemHasZeroQuantity_ShouldReturnStatus400() throws Exception {
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(1L)
                .quantity(0L)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(2L)
                .quantity(1L)
                .build();
        var request = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName(value = """
            In the POST /v1/orders request body, any orderItem in the orderItems array
            has negative quantity value,
            should respond with HTTP status 400
            """)
    void registerOrder_WhenAnOrderItemHasNegativeQuantity_ShouldReturnStatus400() throws Exception {
        var orderItemRequest0 = OrderItemRequest.builder()
                .productId(1L)
                .quantity(-1L)
                .build();
        var orderItemRequest1 = OrderItemRequest.builder()
                .productId(2L)
                .quantity(1L)
                .build();
        var request = OrderRequest.builder()
                .orderItems(List.of(orderItemRequest0, orderItemRequest1))
                .build();

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(ORDER_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(ORDER_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(orderService);
    }
}