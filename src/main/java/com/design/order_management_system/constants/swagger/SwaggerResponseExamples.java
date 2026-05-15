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
}
