package com.design.order_management_system.controller;

import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.dto.response.ProductResponse;
import com.design.order_management_system.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableMethodSecurity
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName(value = "POST /v1/products by a user with the ADMIN role should respond with HTTP status 201")
    void registerProduct_WithAdminRole_ShouldReturn201() throws Exception {
        String productName = "P0";
        BigDecimal price = BigDecimal.valueOf(1.0);
        Long stock = 0L;

        var request = new CreateProductRequest();
        request.setProductName(productName);
        request.setPrice(price);
        request.setStock(stock);

        var response = ProductResponse.builder()
                .productId(1L)
                .productName(productName)
                .price(price)
                .stock(stock)
                .build();

        when(productService.registerProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sProductId").value(1L))
                .andExpect(jsonPath("$.sProductName").value(productName))
                .andExpect(jsonPath("$.dPrice").value(price))
                .andExpect(jsonPath("$.dStock").value(stock));

        verify(productService).registerProduct(any(CreateProductRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName(value = "POST /v1/products by a user with the USER role should respond with HTTP status 403")
    void registerProduct_WithUserRole_ShouldReturn403() throws Exception {
        String productName = "P0";
        BigDecimal price = BigDecimal.valueOf(1.0);
        Long stock = 0L;

        var request = new CreateProductRequest();
        request.setProductName(productName);
        request.setPrice(price);
        request.setStock(stock);

        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }
}