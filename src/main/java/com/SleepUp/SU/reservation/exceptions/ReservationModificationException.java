package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.exceptions.AppException;

public class ReservationModificationException extends AppException {
    public ReservationModificationException(String message) {
        super(message);
    }
}