package com.SleepUp.SU.accommodation.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class AccommodationNotFoundByIdException extends AppException {
    public AccommodationNotFoundByIdException(Long attributeValue) {
        super(ExceptionsMessageHelper.entityNotFound("Accommodation", "id", attributeValue.toString()));
    }
}