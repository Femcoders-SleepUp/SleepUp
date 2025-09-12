package com.SleepUp.SU.auth.dto;

public record AuthResponse(
        String message,
        String tokenType,
        String token,
        String username,
        String refreshToken) {
}