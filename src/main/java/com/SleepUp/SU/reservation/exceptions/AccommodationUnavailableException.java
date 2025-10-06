package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.reservation.dto.ReservationRequest;

public class AccommodationUnavailableException extends AppException {
    public AccommodationUnavailableException(Accommodation accommodation, ReservationRequest reservationRequest) {
        super(String.format("Accommodation is only available from %s to %s", accommodation.getAvailableFrom(), accommodation.getAvailableTo()));
    }
}
