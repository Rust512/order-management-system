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

    public static final String GET_PRODUCTS = """
            {
                "aContent": [
                    {
                        "sProductId": 1,
                        "sProductName": "Protein bar",
                        "dPrice": 20,
                        "dStock": 5
                    },
                    {
                        "sProductId": 2,
                        "sProductName": "Potato Chips",
                        "dPrice": 15,
                        "dStock": 10
                    }
                ],
                "dPage": 1,
                "dSize": 2,
                "dTotalElements": 5,
                "dTotalPages": 3
            }
            """;

    public static final String GET_PRODUCT_AUDIT_ENTRIES = """
            {
                "aContent": [
                    {
                        "dVersion": 1,
                        "sProductName": "Coca-Cola",
                        "dPrice": 15,
                        "dStock": 15,
                        "sOperationType": "CREATE",
                        "dChangedByUserId": 1,
                        "dChangedByUserName": "JohnDoe",
                        "dtCreatedAt": "2026-05-19T08:16:51.007Z"
                    },
                    {
                        "dVersion": 2,
                        "sProductName": "Lassi",
                        "dPrice": 20,
                        "dStock": 20,
                        "sOperationType": "UPDATE",
                        "dChangedByUserId": 2,
                        "dChangedByUserName": "FooBar",
                        "dtCreatedAt": "2026-04-19T08:12:52.107Z"
                    }
                ],
                "dPage": 1,
                "dSize": 2,
                "dTotalElements": 5,
                "dTotalPages": 3
            }
            """;

    public static final String GET_PRODUCT_AUDIT_ENTRY = """
            {
                "dVersion": 1,
                "sProductName": "Coca-Cola",
                "dPrice": 35,
                "dStock": 50,
                "sOperationType": "CREATE",
                "dChangedByUserId": 1,
                "dChangedByUserName": "JohnDoe",
                "dtCreatedAt": "2026-05-19T08:16:51.007Z"
            }
            """;
}
