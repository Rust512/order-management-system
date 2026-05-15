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
                "sUsername": "JohnDoe",
                "sPassword": "Some$Password@432"
            }
            """;
    public static final String PRODUCT_REGISTRATION = """
            {
                "sProductName": "Sprite",
                "dPrice": 29.99,
                "dStock": 5
            }
            """;

    public static final String UPDATE_PRODUCT = """
            {
                "sNewProductName": "Diet Sprite",
                "dUpdatedPrice": 34.99,
                "dStockToAdd": 5
            }
            """;

    public static final String REGISTER_ORDER = """
            {
                "aOrderItems": [
                    {
                        "dProductId": 1,
                        "dQuantity": 1
                    },
                    {
                        "dProductId": 2,
                        "dQuantity": 1
                    }
                ]
            }
            """;
}
