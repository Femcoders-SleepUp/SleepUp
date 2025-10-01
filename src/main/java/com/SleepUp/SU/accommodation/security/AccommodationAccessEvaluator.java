package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationAccessEvaluator {
    private final AccommodationServiceHelper accommodationServiceHelper;

    public boolean isOwner(Long accommodationId, Long userId) {
        boolean owner = accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId);
        if (!owner) {
            throw new AccessDeniedException(
                    "User ID " + userId + " cannot access Accommodation ID " + accommodationId +
                            ". Only the owner is authorized to access this resource."
            );
        }
        return true;
    }
}
