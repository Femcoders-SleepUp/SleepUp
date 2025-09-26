package com.SleepUp.SU.security.config;

import java.util.Arrays;

public class ApiPrefixHelper {
    private static final String PREFIX = "/api/v1";

    public static String[] prefixPaths(String... paths) {
        return Arrays.stream(paths)
                .map(path -> PREFIX + path)
                .toArray(String[]::new);
    }
}
