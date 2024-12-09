package com.example.until;

public class RedisKeyUtils {
    
    private static final String DEFAULT_PREFIX = "APP";

    public static String buildKey(String prefix, String id) {
        return String.format("%s_%s", prefix, id);
    }

    public static String buildKey(String id) {
        return buildKey(DEFAULT_PREFIX, id);
    }
}
