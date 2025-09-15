package com.SleepUp.SU.security.dto;

public record TokenPairAndExpiration(String accessToken, String refreshToken, long jwtExpirationMs) {}

