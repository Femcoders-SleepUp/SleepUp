package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.exceptions.AppException;

public class ReservationAccommodationOwnerException extends AppException {
    public ReservationAccommodationOwnerException( ) {
        super("This accommodation is yours, you cannot book your own accommodations.");
    }
}
