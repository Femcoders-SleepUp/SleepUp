package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationAccessEvaluator {
    private final AccommodationRepository accommodationRepository;

    public boolean isOwner(Long accommodationId, Long userId) {
        boolean owner = accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId);
        if (!owner) {
            throw new AccessDeniedException(
                    "User ID " + userId + " cannot access Accommodation ID " + accommodationId +
                            ". Only the owner is authorized to access this resource."
            );
        }
        return true;
    }
}
