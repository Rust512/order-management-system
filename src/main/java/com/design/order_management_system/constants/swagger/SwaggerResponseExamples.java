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
}
