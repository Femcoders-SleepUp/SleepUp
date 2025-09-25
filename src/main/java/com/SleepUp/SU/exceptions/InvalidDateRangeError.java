package com.SleepUp.SU.exceptions;

public enum InvalidDateRangeError {
    PAST("Check-in date cannot be in the past"),
    ORDER("Check-in date must be before check-out date");

    private final String message;

    InvalidDateRangeError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
