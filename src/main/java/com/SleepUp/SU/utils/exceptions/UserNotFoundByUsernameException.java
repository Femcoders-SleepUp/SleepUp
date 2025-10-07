package com.SleepUp.SU.utils.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class UserNotFoundByUsernameException extends AppException {
    public UserNotFoundByUsernameException(String attributeValue) {
        super(ExceptionsMessageHelper.entityNotFound("User", "id", attributeValue));
    }
}