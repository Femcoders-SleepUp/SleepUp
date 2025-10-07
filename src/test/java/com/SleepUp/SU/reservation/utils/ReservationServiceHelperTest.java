package com.SleepUp.SU.reservation.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.exceptions.InvalidDateRangeException;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.*;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceHelperTest {

    @InjectMocks
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private ReservationRepository reservationRepository;

    @Nested
    class ValidateReservationDates {

        @Test
        void validateReservationDates_validRequest_shouldReturnSuccessfully() {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            assertDoesNotThrow(() -> reservationServiceHelper.validateReservationDates(request));
        }

        @Test
        void validateReservationDates_checkInAfterCheckOut_shouldThrowException() {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(1)
            );

            InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                    () -> reservationServiceHelper.validateReservationDates(request));

            assertEquals("Check-in date must be before check-out date", exception.getMessage());
        }

        @Test
        void validateReservationDates_checkInEqualsCheckOut_shouldThrowException() {
            LocalDate sameDate = LocalDate.now().plusDays(1);
            ReservationRequest request = new ReservationRequest(2, sameDate, sameDate);

            InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                    () -> reservationServiceHelper.validateReservationDates(request));

            assertEquals("Check-in date must be before check-out date", exception.getMessage());
        }

        @Test
        void validateReservationDates_checkInInPast_shouldThrowException() {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1)
            );

            InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                    () -> reservationServiceHelper.validateReservationDates(request));

            assertEquals("Check-in date cannot be in the past", exception.getMessage());
        }
    }

    @Nested
    class ValidateAccommodationAvailability {

        @Test
        void validateAccommodationAvailability_validRequest_shouldReturnSuccessfully() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    3,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(10)
            );

            assertDoesNotThrow(() -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));
        }

        @Test
        void validateAccommodationAvailability_checkInBeforeAvailable_shouldThrowException() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5)
            );

            AccommodationUnavailableException exception = assertThrows(AccommodationUnavailableException.class,
                    () -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));

            assertTrue(exception.getMessage().contains("Accommodation is only available from"));
        }

        @Test
        void validateAccommodationAvailability_checkOutAfterAvailable_shouldThrowException() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(35)
            );

            AccommodationUnavailableException exception = assertThrows(AccommodationUnavailableException.class,
                    () -> reservationServiceHelper.validateAccommodationAvailability(accommodation, request));

            assertTrue(exception.getMessage().contains("Accommodation is only available from"));
        }

        @Test
        void validateAccommodationAvailability_guestExceedsCapacity_shouldThrowException() {
            Accommodation accommodation = createAccommodation();
            ReservationRequest request = new ReservationRequest(
                    6,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(10)
            );

            AccommodationConstraintsException exception = assertThrows(AccommodationConstraintsException.class,
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
    class ValidateUserReservationOverlap {

        @Test
        void validateUserReservationOverlap_noOverlap_shouldReturnSuccessfully() {
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
        void validateUserReservationOverlap_overlapExists_shouldThrowException() {
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

            ReservationOverlapException exception = assertThrows(ReservationOverlapException.class,
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
    class ValidateAccommodationReservationOverlap {

        @Test
        void validateAccommodationReservationOverlap_noOverlap_shouldReturnSuccessfully() {
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
        void validateAccommodationReservationOverlap_overlapExists_shouldThrowException() {
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

            ReservationOverlapException exception = assertThrows(ReservationOverlapException.class,
                    () -> reservationServiceHelper.validateAccommodationReservationOverlap(accommodationId, request));

            assertTrue(exception.getMessage().contains("The accommodation is already reserved during these dates"));
        }
    }

    @Nested
    class ValidateReservationCancellableTests {

        @Test
        void whenBookingStatusCancelled_shouldThrowReservationModificationException() {
            Reservation reservation = new Reservation();
            reservation.setBookingStatus(BookingStatus.CANCELLED);
            reservation.setCheckInDate(LocalDate.now().plusDays(1));

            ReservationModificationException ex = assertThrows(ReservationModificationException.class,
                    () -> reservationServiceHelper.validateReservationCancellable(reservation));

            assertEquals("Cannot modify a cancelled reservation", ex.getMessage());
        }

        @Test
        void whenBookingStatusConfirmed_shouldThrowReservationModificationException() {
            Reservation reservation = new Reservation();
            reservation.setBookingStatus(BookingStatus.CONFIRMED);
            reservation.setCheckInDate(LocalDate.now().plusDays(1));

            ReservationModificationException ex = assertThrows(ReservationModificationException.class,
                    () -> reservationServiceHelper.validateReservationCancellable(reservation));

            assertEquals("Confirmed reservations cannot be cancelled", ex.getMessage());
        }

        @Test
        void whenCheckInDateBeforeNow_shouldThrowReservationModificationException() {
            Reservation reservation = new Reservation();
            reservation.setBookingStatus(BookingStatus.PENDING);
            reservation.setCheckInDate(LocalDate.now().minusDays(1));

            ReservationModificationException ex = assertThrows(ReservationModificationException.class,
                    () -> reservationServiceHelper.validateReservationCancellable(reservation));

            assertEquals("Cannot modify a reservation that has already started", ex.getMessage());
        }

        @Test
        void whenBookingStatusPendingAndCheckInDateFuture_shouldNotThrow() {
            Reservation reservation = new Reservation();
            reservation.setBookingStatus(BookingStatus.PENDING);
            reservation.setCheckInDate(LocalDate.now().plusDays(1));

            assertDoesNotThrow(() -> reservationServiceHelper.validateReservationCancellable(reservation));
        }
    }

    @Nested
    class ValidateGuestIsNotOwnerTests {

        @Test
        void whenUserIsOwner_shouldThrowReservationAccommodationOwnerException() {
            User owner = new User();
            owner.setId(1L);

            Accommodation accommodation = new Accommodation();
            accommodation.setManagedBy(owner);

            User guestWithSameId = new User();
            guestWithSameId.setId(1L);

            assertThrows(ReservationAccommodationOwnerException.class,
                    () -> reservationServiceHelper.validateGuestIsNotOwner(accommodation, guestWithSameId));
        }

        @Test
        void whenUserIsNotOwner_shouldNotThrow() {
            User owner = new User();
            owner.setId(1L);

            Accommodation accommodation = new Accommodation();
            accommodation.setManagedBy(owner);

            User differentGuest = new User();
            differentGuest.setId(2L);

            assertDoesNotThrow(() ->
                    reservationServiceHelper.validateGuestIsNotOwner(accommodation, differentGuest));
        }
    }
}