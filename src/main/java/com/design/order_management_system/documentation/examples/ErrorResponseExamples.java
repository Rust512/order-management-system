package com.design.order_management_system.documentation.examples;

public class ErrorResponseExamples {
    private ErrorResponseExamples() {
    }

    public static final String INVALID_CREDENTIALS = """
            {
                "dStatusCode": 401,
                "sError": "Unauthorized",
                "sExceptionName": "InvalidCredentialsException",
                "sMessage": "Invalid username or password.",
                "sPath": "/auth/login",
                "dtTimeStamp": "2026-05-15T01:48:56.930Z"
            }
            """;
    public static final String INVALID_TOKEN = """
            {
              "dStatusCode": 401,
              "sError": "Unauthorized",
              "sExceptionName": "SignatureException",
              "sMessage": "Invalid JWT Token",
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
                "dtTimeStamp": "2026-05-15T02:11:26.925Z"
            }
            """;
    public static final String USER_ALREADY_EXISTS = """
            {
                "dStatusCode": 409,
                "sError": "Conflict",
                "sExceptionName": "DuplicateResourceException",
                "sMessage": "Resource USER with username matching ADMIN already exists",
                "sPath": "/v1/user",
                "dtTimeStamp": "2026-05-15T03:29:47.680Z"
            }
            """;
    public static final String PRODUCT_ALREADY_EXISTS = """
            {
                "dStatusCode": 409,
                "sError": "Conflict",
                "sExceptionName": "DuplicateResourceException",
                "sMessage": "Resource PRODUCT with name matching Protein bar already exists",
                "sPath": "/v1/products",
                "dtTimeStamp": "2026-05-15T03:29:47.680Z"
            }
            """;
    public static final String PRODUCT_NOT_FOUND = """
            {
                "dStatusCode": 404,
                "sError": "Not Found",
                "sExceptionName": "ResourceNotFoundException",
                "sMessage": "Resource PRODUCT with id matching 5 not found",
                "sPath": "/v1/products",
                "dtTimeStamp": "2026-05-15T02:36:25.603Z"
            }
            """;

    public static final String PRODUCT_NOT_FOUND_FOR_AUDIT = """
            {
                "dStatusCode": 404,
                "sError": "Not Found",
                "sExceptionName": "ResourceNotFoundException",
                "sMessage": "Resource PRODUCT with id matching 5 not found",
                "sPath": "/v1/products/5/audit",
                "dtTimeStamp": "2026-05-15T02:36:25.603Z"
            }
            """;

    public static final String PRODUCT_AUDIT_ENTRY_NOT_FOUND = """
            {
                "dStatusCode": 404,
                "sError": "Not Found",
                "sExceptionName": "ResourceNotFoundException",
                "sMessage": "Resource PRODUCT_AUDIT_ENTRY with (productId, version) matching (5, 3) not found",
                "sPath": "/v1/products/5/audit/3",
                "dtTimeStamp": "2026-05-15T02:36:25.603Z"
            }
            """;

    public static final String ORDER_NOT_FOUND = """
            {
                "dStatusCode": 404,
                "sError": "Not Found",
                "sExceptionName": "ResourceNotFoundException",
                "sMessage": "Resource ORDER with id matching 3 not found",
                "sPath": "/v1/orders",
                "dtTimeStamp": "2026-05-15T02:36:25.603Z"
            }
            """;

    public static final String ORDER_AUDIT_ENTRIES_NOT_FOUND = """
            {
                "dStatusCode": 404,
                "sError": "Not Found",
                "sExceptionName": "ResourceNotFoundException",
                "sMessage": "Resource ORDER_AUDIT_ENTRIES with orderId matching 3 not found",
                "sPath": "/v1/orders/3/audit",
                "dtTimeStamp": "2026-05-15T02:36:25.603Z"
            }
            """;

    public static final String ORDER_AUDIT_ENTRY_NOT_OWNED = """
            {
                "dStatusCode": 403,
                "sError": "Forbidden",
                "sExceptionName": "ResourceNotOwnedException",
                "sMessage": "Resource ORDER_AUDIT_ENTRIES with (orderId, version) matching (3, 2) not owned by user with ID 1",
                "sPath": "/v1/orders/3/audit/2",
                "dtTimeStamp": "2026-05-15T02:36:25.603Z"
            }
            """;


    public static final String INSUFFICIENT_RESOURCES = """
            {
                "dStatusCode": 422,
                "sError": "Unprocessable Content",
                "sExceptionName": "InsufficientResourcesException",
                "sMessage": "Insufficient resources for PRODUCT: requested value of stock was 10, but only 4 was found",
                "sPath": "/v1/orders",
                "dtTimeStamp": "2026-05-15T02:37:32.394Z"
            }
            """;

    public static final String BAD_REQUEST = """
            {
                "dStatusCode": 400,
                "sError": "Unprocessable Content",
                "sExceptionName": "MethodArgumentNotValidException",
                "sMessage": "Validation failed",
                "sPath": "/v1/orders",
                "dtTimeStamp": "2026-05-15T02:37:32.394Z"
            }
            """;
}
