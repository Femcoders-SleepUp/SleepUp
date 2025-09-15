package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationServiceHelper {
    private final AccommodationRepository accommodationRepository;

    public void validateAccommodationNameDoesNotExist(String name) {
        if (accommodationRepository.existsByName(name)) {
            throw new IllegalArgumentException("already exists username");
        }
    }
}