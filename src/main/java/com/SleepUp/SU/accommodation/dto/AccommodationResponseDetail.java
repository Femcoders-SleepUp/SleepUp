package com.SleepUp.SU.accommodation.dto;

public record AccommodationResponseDetail(
        Long id,
        String name,
        Double price,
        int guestNumber,
        boolean petFriendly,
        String location,
        String description,
        String checkInTime,
        String checkOutTime,
        String availableFrom,
        String availableTo,
        String managedByUsername,
        String imageUrl
) {
}