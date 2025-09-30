package com.SleepUp.SU.exceptions;

import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
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

    @Test
    public void testHandleInvalidDateRangeException() {
        InvalidDateRangeException ex = new InvalidDateRangeException(InvalidDateRangeError.ORDER);
        ResponseEntity<ErrorResponse> response = handler.handleInvalidDateRangeException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((String)response.getBody().message()).contains(InvalidDateRangeError.ORDER.getMessage()));
    }

    @Test
    public void testHandleUserNotFoundByIdException() {
        UserNotFoundByIdException ex = new UserNotFoundByIdException(1L);
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundByIdeException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleUserNotFoundByUsernameException() {
        UserNotFoundByUsernameException ex = new UserNotFoundByUsernameException("User not found by username");
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundByUsernameException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleUserEmailAlreadyExists() {
        UserEmailAlreadyExistsException ex = new UserEmailAlreadyExistsException("Email exists");
        ResponseEntity<ErrorResponse> response = handler.handleUserEmailAlreadyExists(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleUserUsernameAlreadyExists() {
        UserUsernameAlreadyExistsException ex = new UserUsernameAlreadyExistsException("Username exists");
        ResponseEntity<ErrorResponse> response = handler.handleUserUsernameAlreadyExists(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleAccommodationNotFoundByIdException() {
        AccommodationNotFoundByIdException ex = new AccommodationNotFoundByIdException(1L);
        ResponseEntity<ErrorResponse> response = handler.handleAccommodationNotFoundByIdException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleAccommodationAlreadyExistsByName() {
        AccommodationAlreadyExistsByNameException ex = new AccommodationAlreadyExistsByNameException("Already exists");
        ResponseEntity<ErrorResponse> response = handler.handleAccommodationAlreadyExistsByName(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleValidationExceptions() {
        FieldError fieldError = new FieldError("obj", "field", "must not be blank");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = (Map<String, String>) response.getBody().message();
        assertTrue(errors.containsKey("field"));
    }

    @Test
    public void testHandleConstraintViolationExceptions() {
        Path path = mock(Path.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().message();

        assertNotNull(errors, "Errors map should not be null");
        assertFalse(errors.isEmpty(), "Errors map should not be empty");
        assertEquals(1, errors.size(), "Errors map size should be 1");

        String errorKey = path.toString();
        assertTrue(errors.containsKey(errorKey), "Errors map should contain key: " + errorKey);
        assertEquals("must not be null", errors.get(errorKey), "Error message should match");
    }

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
        assertTrue(((String)response.getBody().message()).contains("Forbidden"));
    }

    @Test
    public void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(ex, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(((String)response.getBody().message()).contains("Unauthorized"));
    }

    @Test
    public void testHandleReservationNotFoundByIdException() {
        ReservationNotFoundByIdException ex = new ReservationNotFoundByIdException(1L);
        ResponseEntity<ErrorResponse> response = handler.handleReservationNotFoundByIdeException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testHandleAccommodationConstraintsException() {
        AccommodationConstraintsException ex = new AccommodationConstraintsException("Constraint violated");
        ResponseEntity<ErrorResponse> response = handler.handleAccommodationConstraintsException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testHandleAccommodationUnavailableException() {
        AccommodationUnavailableException ex = new AccommodationUnavailableException("Unavailable");
        ResponseEntity<ErrorResponse> response = handler.handleAccommodationUnavailableException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testHandleReservationOverlapException() {
        ReservationOverlapException ex = new ReservationOverlapException("Overlap");
        ResponseEntity<ErrorResponse> response = handler.handleReservationOverlapException(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleReservationModificationException() {
        ReservationModificationException ex = new ReservationModificationException("Modification conflict");
        ResponseEntity<ErrorResponse> response = handler.handleReservationModificationException(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testHandleReservationAccommodationOwnerException() {
        ReservationAccommodationOwnerException ex = new ReservationAccommodationOwnerException();
        ResponseEntity<ErrorResponse> response = handler.handleReservationAccommodationOwnerException(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
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
        assertTrue(errors.containsKey("error"));
        assertEquals("Unexpected error", errors.get("error"));

    }
}
