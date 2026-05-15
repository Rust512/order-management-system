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
                "sProductName": "A premium product",
                "dPrice": 2999.99,
                "dStock": 5
            }
            """;

    public static final String REGISTER_USER = """
            {
                "sUsername": "username",
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
                "dTotalPrice": 45,
                "aOrderItems": [
                    {
                        "sProductName": "Product 0",
                        "dQuantity": 1,
                        "dPurchasePrice": 20
                    },
                    {
                        "sProductName": "Product 1",
                        "dQuantity": 1,
                        "dPurchasePrice": 25
                    }
                ]
            }
            """;

}
