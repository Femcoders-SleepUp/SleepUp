package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.exceptions.AppException;

public class AccommodationUnavailableException extends AppException {
    public AccommodationUnavailableException(String message) {
        super(message);
    }
}
