package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.status.BookingStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ReservationServiceHelperTest {

    @InjectMocks
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private ReservationRepository reservationRepository;

    @Nested
    class ValidateReservationDatesTest {

        @Test
        void should_validateReservationDates_successfully() {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            assertDoesNotThrow(() -> reservationServiceHelper.validateReservationDates(request));
        }

        @Test
        void should_validateReservationDates_throw_exception_when_checkIn_after_checkOut() {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(1)
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateReservationDates(request));

            assertEquals("Check-in date must be before check-out date", exception.getMessage());
        }

        @Test
        void should_validateReservationDates_throw_exception_when_checkIn_equals_checkOut() {
            LocalDate sameDate = LocalDate.now().plusDays(1);
            ReservationRequest request = new ReservationRequest(2, sameDate, sameDate);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateReservationDates(request));

            assertEquals("Check-in date must be before check-out date", exception.getMessage());
        }

        @Test
        void should_validateReservationDates_throw_exception_when_checkIn_in_past() {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1)
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateReservationDates(request));

            assertEquals("Check-in date cannot be in the past", exception.getMessage());
        }
    }

    @Nested
    class ValidateAccommodationAvailabilityTest {

        @Test
        void should_validateAccommodationAvailability_successfully() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    3,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(10)
            );

            assertDoesNotThrow(() -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));
        }

        @Test
        void should_validateAccommodationAvailability_throw_exception_when_checkIn_before_available() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5)
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));

            assertTrue(exception.getMessage().contains("Accommodation is only available from"));
        }

        @Test
        void should_validateAccommodationAvailability_throw_exception_when_checkOut_after_available() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(35)
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));

            assertTrue(exception.getMessage().contains("Accommodation is only available from"));
        }

        @Test
        void should_validateAccommodationAvailability_throw_exception_when_guests_exceed_capacity() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    6,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(10)
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));

            assertTrue(exception.getMessage().contains("Accommodation supports maximum 4 guests, but 6 guests requested"));
        }

        private Accommodation createAccommodation() {
            Accommodation accommodation = new Accommodation();
            accommodation.setId(1L);
            accommodation.setName("Test Hotel");
            accommodation.setGuestNumber(4);
            accommodation.setAvailableFrom(LocalDate.now());
            accommodation.setAvailableTo(LocalDate.now().plusDays(30));
            return accommodation;
        }
    }

    @Nested
    class ValidateUserReservationOverlapTest {

        @Test
        void should_validateUserReservationOverlap_successfully_when_no_overlap() {
            Long userId = 1L;
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            when(reservationRepository.existsOverlappingReservationForUser(
                    userId, request.checkInDate(), request.checkOutDate(), BookingStatus.CANCELLED))
                    .thenReturn(false);

            assertDoesNotThrow(() -> reservationServiceHelper.validateUserReservationOverlap(userId, request));
        }

        @Test
        void should_validateUserReservationOverlap_throw_exception_when_overlap_exists() {
            Long userId = 1L;
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            Reservation overlappingReservation = createOverlappingReservation();
            List<Reservation> overlappingReservations = Collections.singletonList(overlappingReservation);

            when(reservationRepository.existsOverlappingReservationForUser(
                    userId, request.checkInDate(), request.checkOutDate(), BookingStatus.CANCELLED))
                    .thenReturn(true);
            when(reservationRepository.findOverlappingReservationsForUser(
                    userId, request.checkInDate(), request.checkOutDate(), BookingStatus.CANCELLED))
                    .thenReturn(overlappingReservations);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateUserReservationOverlap(userId, request));

            assertTrue(exception.getMessage().contains("You already have a reservation that overlaps with these dates"));
            assertTrue(exception.getMessage().contains("Test Hotel"));
        }

        private Reservation createOverlappingReservation() {
            Reservation reservation = new Reservation();
            reservation.setCheckInDate(LocalDate.now().plusDays(2));
            reservation.setCheckOutDate(LocalDate.now().plusDays(4));

            Accommodation accommodation = new Accommodation();
            accommodation.setName("Test Hotel");
            reservation.setAccommodation(accommodation);

            return reservation;
        }
    }

    @Nested
    class ValidateAccommodationReservationOverlapTest {

        @Test
        void should_validateAccommodationReservationOverlap_successfully_when_no_overlap() {
            Long accommodationId = 1L;
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            when(reservationRepository.existsOverlappingReservationForAccommodation(
                    accommodationId, request.checkInDate(), request.checkOutDate(), BookingStatus.CANCELLED))
                    .thenReturn(false);

            assertDoesNotThrow(() -> reservationServiceHelper.validateAccommodationReservationOverlap(accommodationId, request));
        }

        @Test
        void should_validateAccommodationReservationOverlap_throw_exception_when_overlap_exists() {
            Long accommodationId = 1L;
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            Reservation overlappingReservation = new Reservation();
            overlappingReservation.setCheckInDate(LocalDate.now().plusDays(2));
            overlappingReservation.setCheckOutDate(LocalDate.now().plusDays(4));

            List<Reservation> overlappingReservations = Collections.singletonList(overlappingReservation);

            when(reservationRepository.existsOverlappingReservationForAccommodation(
                    accommodationId, request.checkInDate(), request.checkOutDate(), BookingStatus.CANCELLED))
                    .thenReturn(true);
            when(reservationRepository.findOverlappingReservationsForAccommodation(
                    accommodationId, request.checkInDate(), request.checkOutDate(), BookingStatus.CANCELLED))
                    .thenReturn(overlappingReservations);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceHelper.validateAccommodationReservationOverlap(accommodationId, request));

            assertTrue(exception.getMessage().contains("The accommodation is already reserved during these dates"));
        }
    }
}