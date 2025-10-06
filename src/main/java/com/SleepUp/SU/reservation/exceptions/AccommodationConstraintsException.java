package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.reservation.dto.ReservationRequest;

public class AccommodationConstraintsException extends AppException {
        public AccommodationConstraintsException(Accommodation accommodation, ReservationRequest reservationRequest) {
        super(String.format("Accommodation supports maximum %d guests, but %d guests requested",
                accommodation.getGuestNumber(), reservationRequest.guestNumber()));
    }
}
