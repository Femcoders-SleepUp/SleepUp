package com.SleepUp.SU.reservation.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class ReservationNotFoundByIdException extends AppException {
    public ReservationNotFoundByIdException(Long id) {
        super(ExceptionsMessageHelper.entityNotFound("Reservation", "id", id.toString()));
    }
}