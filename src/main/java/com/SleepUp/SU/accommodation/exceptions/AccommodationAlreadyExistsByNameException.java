package com.SleepUp.SU.accommodation.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class AccommodationAlreadyExistsByNameException extends AppException {
    public AccommodationAlreadyExistsByNameException(String attributeValue) {
        super(ExceptionsMessageHelper.entityAlreadyExists("Accommodation", "name", attributeValue));
    }
}