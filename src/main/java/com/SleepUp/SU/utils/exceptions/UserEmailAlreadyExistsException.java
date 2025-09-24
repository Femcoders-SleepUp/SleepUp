package com.SleepUp.SU.utils.exceptions;

import com.SleepUp.SU.exceptions.AppException;
import com.SleepUp.SU.exceptions.ExceptionsMessageHelper;

public class UserEmailAlreadyExistsException extends AppException {
    public UserEmailAlreadyExistsException(String attributeValue) {
        super(ExceptionsMessageHelper.entityAlreadyExists("User", "email", attributeValue));
    }
}
