package com.SleepUp.SU.accommodation.dto;

public record AccommodationResponseSummary(
        Long id,
        String name,
        Double price,
        int guestNumber,
        boolean petFriendly,
        String location,
        String imageUrl
) {
}