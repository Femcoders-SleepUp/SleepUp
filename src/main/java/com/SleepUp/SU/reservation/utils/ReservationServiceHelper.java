package com.SleepUp.SU.reservation.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.exceptions.AccommodationConstraintsException;
import com.SleepUp.SU.reservation.exceptions.AccommodationUnavailableException;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.exceptions.*;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationServiceHelper {

    private final ReservationRepository reservationRepository;

    public Reservation getReservationEntityById(Long id){
        return reservationRepository.findById(id).orElseThrow(() -> new ReservationNotFoundByIdException(id));
    }

    public boolean isReservationGuestTheUser(Long reservationId, Long userId){
        return reservationRepository.existsByIdAndUser_Id(reservationId, userId);
    }

    public Long getAccommodationIdFromReservationId(Long reservationId){
        return getReservationEntityById(reservationId).getAccommodation().getId();
    }

    public void updatePriceWithDiscountIfDeserved(Reservation reservation, Accommodation accommodation, User user){
        boolean discount = validateReservationAccommodationLessThanOneYear(accommodation.getId(), user.getId());
        BigDecimal amount = calculateReservationPrice(reservation, accommodation, discount);

        reservation.setTotalPrice(amount);
    }

    public BigDecimal calculateReservationPrice(Reservation reservation, Accommodation accommodation, boolean discount) {
        long days = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());

        BigDecimal pricePerDay = BigDecimal.valueOf(accommodation.getPrice());
        BigDecimal totalAmount = pricePerDay.multiply(BigDecimal.valueOf(days));

        if (discount) {
            BigDecimal discountMultiplier = BigDecimal.valueOf(0.8);
            totalAmount = totalAmount.multiply(discountMultiplier);
        }

        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        return totalAmount;
    }

    public void validateCreateReservation(Accommodation accommodation, User user, ReservationRequest reservationRequest){
        validateGuestIsNotOwner(accommodation, user);
        validateAccommodationAvailability(accommodation, reservationRequest);
        validateUserReservationOverlap(user.getId(), reservationRequest);
        validateAccommodationReservationOverlap(accommodation.getId(), reservationRequest);

    }

    public void validateGuestIsNotOwner(Accommodation accommodation, User user) {
        if (accommodation.getManagedBy().getId().equals(user.getId())){
            throw new ReservationAccommodationOwnerException();}
    }

    public void validateReservationDates(ReservationRequest reservationRequest) {
        EntityUtil.validateCheckInOutDates(reservationRequest.checkInDate(), reservationRequest.checkOutDate());
    }

     public void validateAccommodationAvailability(Accommodation accommodation, ReservationRequest reservationRequest) {
        Optional.ofNullable(accommodation)
                .map(Accommodation::getAvailableFrom)
                .filter(availableFrom -> !reservationRequest.checkInDate().isBefore(availableFrom))
                .orElseThrow(() -> new AccommodationUnavailableException(accommodation));

        Optional.ofNullable(accommodation)
                .map(Accommodation::getAvailableTo)
                .filter(availableTo -> !reservationRequest.checkOutDate().isAfter(availableTo))
                .orElseThrow(() -> new AccommodationUnavailableException(accommodation));

        Optional.of(accommodation)
                .filter(acc -> reservationRequest.guestNumber() <= acc.getGuestNumber())
                .orElseThrow(() -> new AccommodationConstraintsException(accommodation, reservationRequest));
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
        Optional.of(reservation.getBookingStatus())
                .filter(status -> status != BookingStatus.CANCELLED)
                .orElseThrow(() -> new ReservationModificationException("Cannot modify a cancelled reservation"));

        Optional.of(reservation.getBookingStatus())
                .filter(status -> status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED)
                .orElseThrow(() -> new ReservationModificationException("Completed reservations cannot be cancelled"));

        Optional.of(reservation.getCheckInDate())
                .filter(date -> date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now()))
                .orElseThrow(() -> new ReservationModificationException("Cannot modify a reservation that has already started"));
    }

    public boolean validateReservationAccommodationLessThanOneYear(Long accommodationId, Long userId){

        LocalDateTime oneYearAgo = LocalDate.now().minusYears(1).atStartOfDay();

        return reservationRepository.existsReservationLessThanYear(
                userId,
                accommodationId,
                oneYearAgo,
                BookingStatus.CANCELLED);
    }
}