package com.SleepUp.SU.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UserRequest(
        @NotBlank(message = "Username cannot be blank") @Size(max = 50, message = "Username cannot be longer than 50 characters!")
        @Schema(description = "The username for user", example = "user1", required = true)
        String username,

        @NotBlank(message = "Name cannot be blank") @Size(max = 100, message = "Name cannot be longer than 100 characters!")
        @Schema(description = "The name for user", example = "Name", required = true)
        String name,

        @NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid!")
        @Schema(description = "The email for user", example = "email@gmail.com", required = true)
        String email,

        @NotBlank(message = "Password cannot be blank") @Size(min = 8, message = "Password should be at least 8 characters long!")
        @Schema(description = "The password for user", example = "password123", required = true)
        String password) {
}
