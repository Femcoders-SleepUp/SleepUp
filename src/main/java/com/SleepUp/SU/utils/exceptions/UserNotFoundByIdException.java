package com.SleepUp.SU.utils.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class UserNotFoundByIdException extends AppException {
    public UserNotFoundByIdException(Long attributeValue) {
        super(ExceptionsMessageHelper.entityNotFound("User", "id", attributeValue.toString()));
    }
}