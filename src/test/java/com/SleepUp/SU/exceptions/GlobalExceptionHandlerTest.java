package com.SleepUp.SU.exceptions;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.exceptions.*;
import com.SleepUp.SU.utils.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");
    }

    // === Grouped Handlers ===

    @Test
    public void testHandleNotFound_UserNotFoundByIdException() {
        UserNotFoundByIdException ex = new UserNotFoundByIdException(1L);
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleNotFound_AccommodationNotFound() {
        AccommodationNotFoundByIdException ex = new AccommodationNotFoundByIdException(1L);
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleConflict_UserEmailAlreadyExists() {
        UserEmailAlreadyExistsException ex = new UserEmailAlreadyExistsException("Email exists");
        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleConflict_AccommodationAlreadyExists() {
        AccommodationAlreadyExistsByNameException ex = new AccommodationAlreadyExistsByNameException("Already exists");
        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleBadRequest_InvalidDateRange() {
        InvalidDateRangeException ex = new InvalidDateRangeException(InvalidDateRangeError.ORDER);
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((String) response.getBody().message()).contains(InvalidDateRangeError.ORDER.getMessage()));
    }

    @Test
    public void testHandleBadRequest_AccommodationUnavailable() {
        Accommodation accommodation = mock(Accommodation.class);
        ReservationRequest reservationRequest = mock(ReservationRequest.class);
        AccommodationUnavailableException ex = new AccommodationUnavailableException(accommodation, reservationRequest);
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String expectedMessage = String.format("Accommodation is only available from %s to %s", accommodation.getAvailableFrom(), accommodation.getAvailableTo());
        assertEquals(expectedMessage, response.getBody().message().toString());
    }

    // === Validation Handlers ===

    @Test
    public void testHandleValidationExceptions() {
        FieldError fieldError = new FieldError("obj", "field", "must not be blank");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(ex.getMessage()).thenReturn("Validation failed");

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().message());
    }

    @Test
    public void testHandleConstraintViolationExceptions() {
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("fieldName");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);
        when(ex.getMessage()).thenReturn("Constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fieldName: must not be null", response.getBody().message());
    }

    // === Special Cases ===

    @Test
    public void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");
        ResponseEntity<ErrorResponse> response = handler.handleEmptyOrMalformedBodyRequest(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body is required and cannot be empty or malformed.", response.getBody().message());
    }

    @Test
    public void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(((String) response.getBody().message()).contains("Access denied"));
    }

    @Test
    public void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(ex, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((String) response.getBody().message()).contains("Unauthorized"));
    }

    @Test
    public void testHandleAllUnhandledExceptions() {
        Exception ex = new Exception("Unknown error");
        ResponseEntity<ErrorResponse> response = handler.handleAllUnhandledExceptions(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> errors = (Map<String, String>) response.getBody().message();
        assertEquals("Unknown error", errors.get("error"));
    }

    @Test
    public void testHandleAllUnhandledExceptions_NoMessage() {
        Exception ex = new Exception();
        ResponseEntity<ErrorResponse> response = handler.handleAllUnhandledExceptions(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> errors = (Map<String, String>) response.getBody().message();
        assertEquals("Unexpected error", errors.get("error"));
    }
}
