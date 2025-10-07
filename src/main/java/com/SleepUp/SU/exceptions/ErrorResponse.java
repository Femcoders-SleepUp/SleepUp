package com.SleepUp.SU.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public record ErrorResponse
        (@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
         ZonedDateTime timestamp,
         int status,
         String error,
         Object message,
         String path){

    public ErrorResponse (HttpStatus status, Object message, HttpServletRequest req){
        this(ZonedDateTime.now(ZoneOffset.UTC), status.value(), status.getReasonPhrase(), message, req.getRequestURI());
    }
}