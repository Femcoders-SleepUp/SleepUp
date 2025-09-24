package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.exceptions.AppException;

public class ReservationInvalidDateException extends AppException {
    public ReservationInvalidDateException(String message) {
        super(message);
    }
}
