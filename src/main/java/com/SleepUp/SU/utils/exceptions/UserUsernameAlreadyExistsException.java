package com.SleepUp.SU.utils.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class UserUsernameAlreadyExistsException extends AppException {
    public UserUsernameAlreadyExistsException(String attributeValue) {
        super(ExceptionsMessageHelper.entityAlreadyExists("User", "username", attributeValue));
    }
}