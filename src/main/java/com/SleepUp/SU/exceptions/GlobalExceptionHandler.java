package com.SleepUp.SU.exceptions;

import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.reservation.exceptions.*;
import com.SleepUp.SU.utils.exceptions.UserEmailAlreadyExistsException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByIdException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByUsernameException;
import com.SleepUp.SU.utils.exceptions.UserUsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotFoundByIdException.class,
            UserNotFoundByUsernameException.class,
            AccommodationNotFoundByIdException.class,
            ReservationNotFoundByIdException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({
            UserEmailAlreadyExistsException.class,
            UserUsernameAlreadyExistsException.class,
            AccommodationAlreadyExistsByNameException.class,
            ReservationOverlapException.class,
            ReservationModificationException.class,
            ReservationAccommodationOwnerException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({
            InvalidDateRangeException.class,
            AccommodationConstraintsException.class,
            AccommodationUnavailableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationExceptions(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations().forEach(error -> {
            String fieldName = error.getPropertyPath().toString();
            String errorMessage = error.getMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleEmptyOrMalformedBodyRequest(HttpMessageNotReadableException exception, HttpServletRequest request) {

        String message = "Request body is required and cannot be empty or malformed.";

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {

        String message = "Access denied. You are not authorized to execute this action." + exception.getMessage();

        return buildResponse(HttpStatus.FORBIDDEN, message, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exception, HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized: Bad credentials", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUnhandledExceptions(Exception exception, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        errors.put("error", exception.getMessage() != null ? exception.getMessage() : "Unexpected error");

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, errors, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, Object message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(status, message, request));
    }

}