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

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRangeException(InvalidDateRangeException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(UserNotFoundByIdException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundByIdeException(UserNotFoundByIdException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(UserNotFoundByUsernameException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundByUsernameException(UserNotFoundByUsernameException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(UserEmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserEmailAlreadyExists(UserEmailAlreadyExistsException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(UserUsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserUsernameAlreadyExists(UserUsernameAlreadyExistsException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccommodationNotFoundByIdException.class)
    public ResponseEntity<ErrorResponse> handleAccommodationNotFoundByIdException(AccommodationNotFoundByIdException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccommodationAlreadyExistsByNameException.class)
    public ResponseEntity<ErrorResponse> handleAccommodationAlreadyExistsByName(AccommodationAlreadyExistsByNameException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(status, "VALIDATION_ERROR", errors, request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationExceptions(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations().forEach(error -> {
            String fieldName = error.getPropertyPath().toString();
            String errorMessage = error.getMessage();
            errors.put(fieldName, errorMessage);
        });
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(status, "CONSTRAINT_VIOLATION", errors, request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleEmptyOrMalformedBodyRequest(HttpMessageNotReadableException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Request body is required and cannot be empty or malformed.";
        ErrorResponse body = new ErrorResponse(status, message, request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorResponse body = new ErrorResponse(status, "Access Denied. You are not authorized to execute this action. " + exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorResponse body = new ErrorResponse(status, "Unauthorized: Bad credentials", request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ReservationNotFoundByIdException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFoundByIdeException(ReservationNotFoundByIdException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccommodationConstraintsException.class)
    public ResponseEntity<ErrorResponse> handleAccommodationConstraintsException(
            AccommodationConstraintsException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AccommodationUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAccommodationUnavailableException(
            AccommodationUnavailableException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }
    @ExceptionHandler(ReservationOverlapException.class)
    public ResponseEntity<ErrorResponse> handleReservationOverlapException(
            ReservationOverlapException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ReservationModificationException.class)
    public ResponseEntity<ErrorResponse> handleReservationModificationException(
            ReservationModificationException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ReservationAccommodationOwnerException.class)
    public ResponseEntity<ErrorResponse> handleReservationAccommodationOwnerException(
            ReservationAccommodationOwnerException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(status, exception.getMessage(), request);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUnhandledExceptions(Exception exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, String> errors = new HashMap<>();
        errors.put("error", exception.getMessage() != null ? exception.getMessage() : "Unexpected error");

        ErrorResponse body = new ErrorResponse(status, errors, request);
        return ResponseEntity.status(status).body(body);
    }
}
