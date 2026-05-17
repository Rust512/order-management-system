package com.design.order_management_system.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
public class HashUtils {
    private HashUtils() {
    }

    public static final String HASH_ALGORITHM = "SHA-256";

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash failed; algorithm={} reason=algorithm_unavailable", HASH_ALGORITHM);
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
