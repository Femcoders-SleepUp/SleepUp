package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.accommodation.security.AccommodationAccessEvaluator;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationAccessEvaluator {
    private final ReservationRepository reservationRepository;
    private final AccommodationAccessEvaluator accommodationAccessEvaluator;
    private final ReservationServiceHelper reservationServiceHelper;

    public boolean isReservationGuest(Long reservationId, Long userId){
        boolean exists = reservationRepository.existsByIdAndUser_Id(reservationId, userId);
        if (!exists) {
            throw new AccessDeniedException(
                    "User ID " + userId + " cannot access Reservation ID " + reservationId +
                            ". Only reservation guests can access this information."
            );

        }
        return true;
    }

    public boolean isReservationGuestOrOwner(Long reservationId, Long userId){
        Long accommodationId = reservationServiceHelper.getReservationEntityById(reservationId).getAccommodation().getId();
        return isReservationGuest(reservationId, userId) || accommodationAccessEvaluator.isOwner(accommodationId, userId);
    }

    public boolean isReservationAccommodationOwner(Long reservationId, Long userId){
        Long accommodationId = reservationServiceHelper.getReservationEntityById(reservationId).getAccommodation().getId();
        return accommodationAccessEvaluator.isOwner(accommodationId, userId);
    }
}
