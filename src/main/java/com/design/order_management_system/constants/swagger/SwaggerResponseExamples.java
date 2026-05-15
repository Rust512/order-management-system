package com.design.order_management_system.constants.swagger;

public class SwaggerResponseExamples {
    private SwaggerResponseExamples() {
    }

    public static final String LOGIN = """
            {
                "sToken": "The JWT token"
            }
            """;

    public static final String REGISTER_PRODUCT = """
            {
                "sProductId": 1,
                "sProductName": "Protein bar",
                "dPrice": 34.99,
                "dStock": 5
            }
            """;

    public static final String REGISTER_USER = """
            {
                "sUsername": "JohnDoe",
                "aRoles": [
                    "ROLE_USER"
                ]
            }
            """;

    public static final String REGISTER_ORDER = """
            {
                "dOrderId": 1,
                "sOrderStatus": "CREATED",
                "dtCreatedAt": "2026-05-15T02:36:03.372348931Z",
                "dTotalPrice": 65,
                "aOrderItems": [
                    {
                        "sProductName": "Potato Chips",
                        "dQuantity": 2,
                        "dPurchasePrice": 20
                    },
                    {
                        "sProductName": "Coca-Cola",
                        "dQuantity": 1,
                        "dPurchasePrice": 25
                    }
                ]
            }
            """;

}
