package com.SleepUp.SU.user.dto;

import com.SleepUp.SU.user.role.Role;

public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        Role role
) {
}
