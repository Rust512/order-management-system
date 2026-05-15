package com.design.order_management_system.constants.swagger;

public class SwaggerResponseExamples {
    private SwaggerResponseExamples() {
    }

    public static final String LOGIN = """
            {
                "sToken": "The JWT token"
            }
            """;

    public static final String INVALID_CREDENTIALS = """
            {
                "dStatusCode": 401,
                "sError": "Unauthorized",
                "sExceptionName": "InvalidCredentialsException",
                "sMessage": "Given password for username ADMIN is invalid",
                "sPath": "/auth/login",
                "dtTimeStamp": "2026-05-15T01:48:56.930701701Z"
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

    public static final String INVALID_TOKEN = """
            {
              "dStatusCode": 401,
              "sError": "Unauthorized",
              "sExceptionName": "SignatureException",
              "sMessage": "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.",
              "sPath": "/v1/products",
              "dtTimeStamp": "2026-05-15T01:45:27.033039428Z"
            }
            """;

    public static final String ACCESS_DENIED = """
            {
                "dStatusCode": 403,
                "sError": "Forbidden",
                "sExceptionName": "AuthorizationDeniedException",
                "sMessage": "Access Denied",
                "sPath": "/v1/products",
                "dtTimeStamp": "2026-05-15T02:11:26.925807628Z"
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
    
    public static final String PRODUCT_NOT_FOUND = """
            {
                "dStatusCode": 404,
                "sError": "Not Found",
                "sExceptionName": "ResourceNotFoundException",
                "sMessage": "Resource PRODUCT with id matching 5 not found",
                "sPath": "/v1/orders",
                "dtTimeStamp": "2026-05-15T02:36:25.603756763Z"
            }
            """;
    
    public static final String INSUFFICIENT_RESOURCES = """
            {
                "dStatusCode": 422,
                "sError": "Unprocessable Content",
                "sExceptionName": "InsufficientResourcesException",
                "sMessage": "Insufficient resources for PRODUCT: requested value of stock was 10, but only 4 was found",
                "sPath": "/v1/orders",
                "dtTimeStamp": "2026-05-15T02:37:32.394464220Z"
            }
            """;
}
