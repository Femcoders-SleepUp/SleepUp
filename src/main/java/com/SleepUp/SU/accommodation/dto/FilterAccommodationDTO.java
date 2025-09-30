package com.SleepUp.SU.accommodation.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record FilterAccommodationDTO(
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @DecimalMin(value = "0.0", inclusive = true, message = "Minimum price must be positive")
        Double minPrice,

        @DecimalMin(value = "0.0", inclusive = true, message = "Maximum price must be positive")
        Double maxPrice,

        @Min(value = 1, message = "Guest number must be at least 1")
        Integer guestNumber,

        String location,

        LocalDate fromDate,
        LocalDate toDate,

        Boolean petFriendly
) {}
