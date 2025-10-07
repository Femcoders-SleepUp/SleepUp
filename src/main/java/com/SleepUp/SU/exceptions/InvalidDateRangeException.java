package com.SleepUp.SU.exceptions;

public class InvalidDateRangeException extends AppException{
    public InvalidDateRangeException(InvalidDateRangeError error) {
        super(error.getMessage());
    }
}