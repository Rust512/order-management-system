package com.design.order_management_system.utils;

import java.util.UUID;

public class GeneratorUtils {
    private GeneratorUtils() {
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
