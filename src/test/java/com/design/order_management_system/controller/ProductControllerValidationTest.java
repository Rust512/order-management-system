package com.design.order_management_system.controller;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.dto.request.CreateProductRequest;
import com.design.order_management_system.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableMethodSecurity
@WithMockUser(roles = "ADMIN")
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private static final String PRODUCT_REGISTRATION_ENDPOINT = "/v1/products";

    @Test
    @DisplayName(value = """
            POST /v1/products with a missing (null) product name
            should return HTTP status 400
            """)
    void registerProduct_WithMissingProductName_ShouldReturn400() throws Exception {
        var request = new CreateProductRequest();
        request.setPrice(BigDecimal.valueOf(1.0));
        request.setStock(0L);

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(PRODUCT_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(PRODUCT_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName(value = """
            POST /v1/products with an empty product name
            should return HTTP status 400
            """)
    void registerProduct_WithBlankProductName_ShouldReturn400() throws Exception {
        var request = new CreateProductRequest();
        request.setProductName(CommonConstants.EMPTY);
        request.setPrice(BigDecimal.valueOf(1.0));
        request.setStock(0L);

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(PRODUCT_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(PRODUCT_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName(value = """
            POST /v1/products with a whitespaces-only product name
            should return HTTP status 400
            """)
    void registerProduct_WithWhitespaceProductName_ShouldReturn400() throws Exception {
        var request = new CreateProductRequest();
        request.setProductName(" \r\n\t\f");
        request.setPrice(BigDecimal.valueOf(1.0));
        request.setStock(0L);

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(PRODUCT_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(PRODUCT_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName(value = """
            POST /v1/products with zero price
            should return HTTP status 400
            """)
    void registerProduct_WithZeroPrice_ShouldReturn400() throws Exception {
        var request = new CreateProductRequest();
        request.setProductName("P0");
        request.setPrice(BigDecimal.ZERO);
        request.setStock(0L);

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(PRODUCT_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(PRODUCT_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName(value = """
            POST /v1/products with negative price
            should return HTTP status 400
            """)
    void registerProduct_WithNegativePrice_ShouldReturn400() throws Exception {
        var request = new CreateProductRequest();
        request.setProductName("P0");
        request.setPrice(BigDecimal.valueOf(-1L));
        request.setStock(0L);

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(PRODUCT_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(PRODUCT_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName(value = """
            POST /v1/products with negative stock
            should return HTTP status 400
            """)
    void registerProduct_WithNegativeStock_ShouldReturn400() throws Exception {
        var request = new CreateProductRequest();
        request.setProductName("P0");
        request.setPrice(BigDecimal.valueOf(1.0));
        request.setStock(-1L);

        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;

        mockMvc.perform(post(PRODUCT_REGISTRATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.dStatusCode").value(expectedStatus.value()))
                .andExpect(jsonPath("$.sError").value(expectedStatus.getReasonPhrase()))
                .andExpect(jsonPath("$.sPath").value(PRODUCT_REGISTRATION_ENDPOINT))
                .andExpect(jsonPath("$.sExceptionName").value(MethodArgumentNotValidException.class.getSimpleName()));

        verifyNoInteractions(productService);
    }
}