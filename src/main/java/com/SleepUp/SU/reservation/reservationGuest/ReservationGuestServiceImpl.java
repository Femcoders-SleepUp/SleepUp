package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.reservation.dto.*;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.service.ReservationService;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ReservationGuestServiceImpl implements ReservationGuestService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EmailServiceHelper emailServiceHelper;
    private final ReservationService reservationService;

    @Override
    public ReservationResponseDetail getReservationById(Long id){
        Reservation isExisting = reservationServiceHelper.getReservationEntityById(id);
        return reservationMapper.toDetail(isExisting);
    }



    @Override
    public ApiMessageDto updateReservation(Long id, ReservationRequest reservationRequest, User user) {
        Reservation oldReservation = reservationServiceHelper.getReservationEntityById(id);
        this.cancelReservation(id);
        ReservationResponseDetail savedReservation = reservationService.createReservation(
                reservationRequest,
                user,
                oldReservation.getAccommodation().getId()
        );

        String message = String.format(
                "Your reservation in %s from %s to %s has been updated.\nOld dates were from %s to %s.",
                savedReservation.accommodationName(),
                savedReservation.checkInDate(),
                savedReservation.checkOutDate(),
                oldReservation.getCheckInDate(),
                oldReservation.getCheckOutDate()
        );

        return new ApiMessageDto(message);
    }

    @Override
    public ApiMessageDto cancelReservation(Long reservationId) {
        Reservation reservation = reservationServiceHelper.getReservationEntityById(reservationId);

        reservationServiceHelper.validateReservationCancellable(reservation);
        reservation.setBookingStatus(BookingStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);

        emailServiceHelper.sendCancellationConfirmationEmail(reservation.getUser(), reservation.getAccommodation(), reservation);
        emailServiceHelper.sendCancellationNotificationToOwnerEmail(reservation.getUser(), reservation.getAccommodation(), reservation);

        String message = String.format(
                "Your reservation in %s from %s to %s has been cancelled",
                savedReservation.getAccommodation().getName(),
                savedReservation.getCheckInDate(),
                savedReservation.getCheckOutDate()
        );

        return new ApiMessageDto(message);
    }

}
