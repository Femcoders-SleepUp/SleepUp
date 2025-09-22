package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.user.security.UserAccessEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationAccessEvaluator {
    private final AccommodationRepository accommodationRepository;
    private final UserAccessEvaluator userAccessEvaluator;

    public boolean isOwner(Long accommodationId, Long userId) {
        return accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId);
    }

    public boolean isOwnerOrAdmin(Long userId, Long accommodationId) {
        return userAccessEvaluator.isAdmin(userId)
                || accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId);
    }

}
