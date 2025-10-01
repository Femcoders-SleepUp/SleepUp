package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
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
                    "User ID " + userId + " cannot access Reservation ID " + reservationId +
                            ". Only reservation guests can access this information."
            );

        }
        return exists;
    }

    public boolean isReservationGuestOrOwner(Long reservationId, Long userId){
        Long accommodationId =  reservationServiceHelper.getAccommodationIdFromReservationId(reservationId);
        return reservationServiceHelper.isReservationGuestTheUser(reservationId, userId) || accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId);
    }

    public boolean isReservationAccommodationOwner(Long reservationId, Long userId){
        Long accommodationId = reservationServiceHelper.getAccommodationIdFromReservationId(reservationId);
        return accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId);
    }
}
