package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.exceptions.AppException;

public class ReservationOverlapException extends AppException {
    public ReservationOverlapException(String message) {
        super(message);
    }
}