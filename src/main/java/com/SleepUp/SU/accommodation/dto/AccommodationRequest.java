package com.SleepUp.SU.accommodation.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record AccommodationRequest (
        @NotEmpty
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Positive
        Double price,

        @Positive
        int guestNumber,

        @NotEmpty
        String location,

        @NotEmpty
        @Size(max = 200)
        String description,

        @NotNull
        LocalTime checkInTime,

        @NotNull
        LocalTime checkOutTime,

        @NotNull
        LocalDate availableFrom,

        @NotNull
        LocalDate availableTo,

        @NotBlank
        String imageUrl
){
}