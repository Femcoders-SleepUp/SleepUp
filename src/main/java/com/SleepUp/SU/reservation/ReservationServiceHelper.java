package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.status.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceHelper {

    private final ReservationRepository reservationRepository;

    public void validateReservationDates(ReservationRequest reservationRequest) {
        if (reservationRequest.checkInDate().isAfter(reservationRequest.checkOutDate()) ||
                reservationRequest.checkInDate().isEqual(reservationRequest.checkOutDate())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        if (reservationRequest.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
    }

    public void validateAccommodationAvailability(Accommodation accommodation, ReservationRequest reservationRequest) {
        LocalDate availableFrom = accommodation.getAvailableFrom();
        LocalDate availableTo = accommodation.getAvailableTo();

        if (reservationRequest.checkInDate().isBefore(availableFrom) ||
                reservationRequest.checkOutDate().isAfter(availableTo)) {
            throw new IllegalArgumentException(
                    String.format("Accommodation is only available from %s to %s",
                            availableFrom, availableTo)
            );
        }

        if (reservationRequest.guestNumber() > accommodation.getGuestNumber()) {
            throw new IllegalArgumentException(
                    String.format("Accommodation supports maximum %d guests, but %d guests requested",
                            accommodation.getGuestNumber(), reservationRequest.guestNumber())
            );
        }
    }

    public void validateUserReservationOverlap(Long userId, ReservationRequest reservationRequest) {
        boolean hasOverlappingReservation = reservationRepository.existsOverlappingReservationForUser(
                userId,
                reservationRequest.checkInDate(),
                reservationRequest.checkOutDate(),
                BookingStatus.CANCELLED
        );

        if (hasOverlappingReservation) {
            List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservationsForUser(
                    userId,
                    reservationRequest.checkInDate(),
                    reservationRequest.checkOutDate(),
                    BookingStatus.CANCELLED
            );

            String conflictDetails = overlappingReservations.stream()
                    .map(r -> String.format("Reservation at %s from %s to %s",
                            r.getAccommodation().getName(),
                            r.getCheckInDate(),
                            r.getCheckOutDate()))
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown conflict");

            throw new IllegalArgumentException(
                    "You already have a reservation that overlaps with these dates: " + conflictDetails
            );
        }
    }

    public void validateAccommodationReservationOverlap(Long accommodationId, ReservationRequest reservationRequest) {
        boolean hasOverlappingReservation = reservationRepository.existsOverlappingReservationForAccommodation(
                accommodationId,
                reservationRequest.checkInDate(),
                reservationRequest.checkOutDate(),
                BookingStatus.CANCELLED
        );

        if (hasOverlappingReservation) {
            List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservationsForAccommodation(
                    accommodationId,
                    reservationRequest.checkInDate(),
                    reservationRequest.checkOutDate(),
                    BookingStatus.CANCELLED
            );

            String conflictDetails = overlappingReservations.stream()
                    .map(r -> String.format("Reserved from %s to %s",
                            r.getCheckInDate(),
                            r.getCheckOutDate()))
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown conflict");

            throw new IllegalArgumentException(
                    "The accommodation is already reserved during these dates: " + conflictDetails
            );
        }
    }

    public Reservation findReservationByIdAndUser(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getUser().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this reservation");
        }

        return reservation;
    }

    public void validateReservationCancellable(Reservation reservation) {
        if (reservation.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot modify a cancelled reservation");
        }

        if (!(reservation.getBookingStatus() == BookingStatus.PENDING) && !(reservation.getBookingStatus() == BookingStatus.CONFIRMED)) {
            throw new RuntimeException("Completed reservations cannot be cancelled");
        }

        if (reservation.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot modify a reservation that has already started");
        }
    }
}