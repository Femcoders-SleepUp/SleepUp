package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationAccessEvaluator {
    private final ReservationRepository reservationRepository;

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
}
