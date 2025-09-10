package com.SleepUp.SU.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public record AuthResponse(
        String message,
        String token,
        String username,
        String refreshToken) {
}
