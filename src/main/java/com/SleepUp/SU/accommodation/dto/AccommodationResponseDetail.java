package com.SleepUp.SU.accommodation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record AccommodationResponseDetail(
        Long id,
        String name,
        Double price,
        int guestNumber,
        boolean petFriendly,
        String location,
        String description,
        @JsonFormat(pattern = "HH:mm")
        LocalTime checkInTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime checkOutTime,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate availableFrom,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate availableTo,
        String managedByUsername,
        String imageUrl
) {
}