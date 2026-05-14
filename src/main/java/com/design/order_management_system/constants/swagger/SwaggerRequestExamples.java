package com.design.order_management_system.constants.swagger;

public class SwaggerRequestExamples {
    private SwaggerRequestExamples() {
    }

    public static final String LOGIN = """
            {
                "sUsername": "ADMIN",
                "sPassword": "Admin@123"
            }
            """;
    public static final String USER_REGISTRATION = """
            {
                "sUsername": "username",
                "sPassword": "Some$Password@432"
            }
            """;
    public static final String PRODUCT_REGISTRATION = """
            {
                "sProductName": "Product 0",
                "dPrice": 200.54,
                "dStock": 5
            }
            """;

    public static final String UPDATE_PRODUCT = """
            {
                "sNewProductName": "Product 0 PRO",
                "dUpdatedPrice": 349.99,
                "dStockToAdd": 5
            }
            """;
}
