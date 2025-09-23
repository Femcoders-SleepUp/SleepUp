package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationAccessEvaluator {
    private final AccommodationRepository accommodationRepository;

    public boolean isOwner(Long accommodationId, Long userId) {
        boolean owner = accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId);
        System.out.println("Checking ownership of accommodation " + accommodationId + " by user " + userId + ": " + owner);
        return owner;
    }


}
