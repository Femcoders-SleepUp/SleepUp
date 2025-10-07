package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.ReservationModificationException;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.utils.email.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationOwnerServiceImpl implements ReservationOwnerService{

    private final ReservationRepository reservationRepository;
    private final EntityUtil entityUtil;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EmailService emailService;

    @Override
    public List<ReservationResponseSummary> getReservationsForMyAccommodation(Long accommodationId) {
        List<Reservation> reservations = reservationRepository.findByAccommodationId(accommodationId);
        return entityUtil.mapEntitiesToDTOs(reservations, reservationMapper::toSummary);
    }

    @Override
    @Transactional
    public ReservationResponseDetail updateStatus(Long id, ReservationAuthRequest reservationAuthRequest) {
        Reservation existingReservation = reservationServiceHelper.getReservationEntityById(id);
        BookingStatus currentStatus = existingReservation.getBookingStatus();
        BookingStatus newStatus = reservationAuthRequest.bookingStatus();

        Optional.of(currentStatus)
                .filter(status -> status != BookingStatus.CANCELLED)
                .orElseThrow(() -> new ReservationModificationException("Cannot modify a cancelled reservation"));

        Optional.of(newStatus)
                .filter(status -> !status.equals(currentStatus))
                .ifPresent(status -> {
                    existingReservation.setBookingStatus(status);
                    BigDecimal amount = existingReservation.getTotalPrice();

                    Optional.of(status)
                            .filter(s -> s == BookingStatus.CONFIRMED)
                            .ifPresent(s -> emailService.sendGuestReservationConfirmationEmail(existingReservation, amount));

                    Optional.of(status)
                            .filter(s -> s == BookingStatus.CANCELLED)
                            .ifPresent(s -> emailService.sendCancellationByOwnerNotificationEmail(existingReservation));
                });

        return reservationMapper.toDetail(existingReservation);
    }

}