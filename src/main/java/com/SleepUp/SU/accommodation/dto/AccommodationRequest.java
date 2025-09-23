package com.SleepUp.SU.accommodation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record AccommodationRequest (
        @NotBlank(message = "Name must be between 2 and 100 characters")
        @Size(min = 2, max = 100)
        String name,

        @NotNull(message = "Price must be provided")
        @Positive(message = "Price must be positive")
        Double price,

        @Positive(message = "Guest number must be positive")
        int guestNumber,

        @NotNull(message = "Pet friendly must be specified")
        Boolean petFriendly,

        @NotBlank(message = "Location must not be blank")
        String location,

        @NotBlank(message = "Description must not be blank")
        @Size(max = 200, message = "Description max length is 200 characters")
        String description,

        @NotNull(message = "Check-in time must be specified in HH:mm format")
        @JsonFormat(pattern = "HH:mm")
        LocalTime checkInTime,

        @NotNull(message = "Check-out time must be specified in HH:mm format")
        @JsonFormat(pattern = "HH:mm")
        LocalTime checkOutTime,

        @NotNull(message = "Available from date must be specified in yyyy-MM-dd format")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate availableFrom,

        @NotNull(message = "Available to date must be specified in yyyy-MM-dd format")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate availableTo,

        @NotBlank(message = "Image URL must not be blank")
        String imageUrl
){
}
