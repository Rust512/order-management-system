package com.design.order_management_system.constants;

public class ErrorMessageConstants {
    public static final String ALREADY_EXISTS = "Resource %s with %s matching %s already exists";
    public static final String INSUFFICIENT_RESOURCES = "Insufficient resources for %s: requested value of %s was %d, but only %d was found";
    public static final String PASSWORD_IS_INVALID = "Given credentials for username %s are invalid.";
    public static final String RESOURCE_NOT_FOUND = "Resource %s with %s matching %s not found";

    private ErrorMessageConstants() {
    }

    public static final String PRODUCT_STOCK_CANNOT_BE_NEGATIVE = "Product stock cannot be negative";
}
