package com.SleepUp.SU.reservation.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.exceptions.AccommodationConstraintsException;
import com.SleepUp.SU.reservation.exceptions.AccommodationUnavailableException;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.exceptions.*;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceHelper {

    private final ReservationRepository reservationRepository;

    public Reservation getReservationEntityById(Long id){
        return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundByIdException(id));
    }

    public void validateReservationDates(ReservationRequest reservationRequest) {
        EntityUtil.validateCheckInOutDates(reservationRequest.checkInDate(), reservationRequest.checkOutDate());
    }

    public void validateAccommodationAvailability(Accommodation accommodation, ReservationRequest reservationRequest) {
        LocalDate availableFrom = accommodation.getAvailableFrom();
        LocalDate availableTo = accommodation.getAvailableTo();

        if (reservationRequest.checkInDate().isBefore(availableFrom) ||
                reservationRequest.checkOutDate().isAfter(availableTo)) {
            throw new AccommodationUnavailableException(
                    String.format("Accommodation is only available from %s to %s",
                            availableFrom, availableTo)
            );
        }

        if (reservationRequest.guestNumber() > accommodation.getGuestNumber()) {
            throw new AccommodationConstraintsException(
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

            throw new ReservationOverlapException(
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

            throw new ReservationOverlapException(
                    "The accommodation is already reserved during these dates: " + conflictDetails
            );
        }
    }

    public void validateReservationCancellable(Reservation reservation) {
        if (reservation.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ReservationModificationException("Cannot modify a cancelled reservation");
        }

        if (!(reservation.getBookingStatus() == BookingStatus.PENDING) && !(reservation.getBookingStatus() == BookingStatus.CONFIRMED)) {
            throw new ReservationModificationException("Completed reservations cannot be cancelled");
        }

        if (reservation.getCheckInDate().isBefore(LocalDate.now())) {
            throw new ReservationModificationException("Cannot modify a reservation that has already started");
        }
    }

    public boolean validateReservationAccommodationLessThanOneYear(Long accommodationId, Long userId){

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

            return reservationRepository.existsReservationLessThanYear(
                    userId,
                    accommodationId,
                    oneYearAgo,
                    BookingStatus.CANCELLED);
    }
}