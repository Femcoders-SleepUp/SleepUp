package com.SleepUp.SU.accommodation.dto;

public record AccommodationResponseSummary(
        String name,
        Double price,
        int guestNumber,
        String location,
        String imageUrl
) {
}