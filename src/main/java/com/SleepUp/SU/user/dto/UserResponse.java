package com.SleepUp.SU.user.dto;

public record UserResponse(
        Long id,
        String username,
        String email,
        java.util.Set<String> roles
) {
}
