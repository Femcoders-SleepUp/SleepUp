package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationAccessEvaluator {
    private final AccommodationServiceHelper accommodationServiceHelper;
    private final ReservationServiceHelper reservationServiceHelper;

    public boolean isReservationGuest(Long reservationId, Long userId){
        boolean exists = reservationServiceHelper.isReservationGuestTheUser(reservationId, userId);
        if (!exists) {
            throw new AccessDeniedException(
                    " Cause: This reservation was not created by you."
            );

        }
        return exists;
    }

    public boolean isReservationGuestOrOwner(Long reservationId, Long userId){
        Long accommodationId =  reservationServiceHelper.getAccommodationIdFromReservationId(reservationId);
        boolean exists = reservationServiceHelper.isReservationGuestTheUser(reservationId, userId) || accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId);
        if (!exists) {
            throw new AccessDeniedException(
                    " Cause: This reservation was not created by you or does not belong to any of your accommodations."
            );

        }
        return exists;
    }

    public boolean isReservationAccommodationOwner(Long reservationId, Long userId){
        Long accommodationId = reservationServiceHelper.getAccommodationIdFromReservationId(reservationId);
        boolean exists = accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId);
        if (!exists) {
            throw new AccessDeniedException(
                    " Cause: This reservation does not belong to any of your accommodations."
            );

        }
        return exists;
    }
}