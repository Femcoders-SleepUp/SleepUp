package com.SleepUp.SU.accommodation.dto;

import lombok.Builder;

@Builder
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