package com.SleepUp.SU.reservation.service;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.ReservationAccommodationOwnerException;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.reservationtime.ReservationTime;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService{

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EmailServiceHelper emailServiceHelper;
    private final AccommodationServiceHelper accommodationServiceHelper;
    private final EntityUtil entityUtil;

    @Override
    public List<ReservationResponseSummary> getMyReservations(Long userId, ReservationTime time) {
        LocalDate today = LocalDate.now();

        if (time == null) {
            time = ReservationTime.ALL;
        }

        List<Reservation> reservations = switch (time) {
            case ALL -> reservationRepository.findByUser_Id(userId);
            case PAST -> reservationRepository.findByUser_IdAndCheckInDateBefore(userId, today);
            case FUTURE -> reservationRepository.findByUser_IdAndCheckInDateAfter(userId, today);
        };

        return entityUtil.mapEntitiesToDTOs(reservations, reservationMapper::toSummary);
    }

    @Override
    public ReservationResponseDetail createReservation(ReservationRequest reservationRequest, User user, Long accommodationId){
        reservationServiceHelper.validateReservationDates(reservationRequest);
        Accommodation accommodation = accommodationServiceHelper.getAccommodationEntityById(accommodationId);

        if (accommodation.getManagedBy().getId().equals(user.getId())){throw new ReservationAccommodationOwnerException();}
        reservationServiceHelper.validateAccommodationAvailability(accommodation, reservationRequest);
        reservationServiceHelper.validateUserReservationOverlap(user.getId(), reservationRequest);
        reservationServiceHelper.validateAccommodationReservationOverlap(accommodationId, reservationRequest);

        Reservation newReservation =  reservationMapper.toEntity(
                reservationRequest,
                BookingStatus.PENDING,
                user, accommodation,
                false);

        Reservation savedReservation = reservationRepository.save(newReservation);

        emailServiceHelper.sendOwnerReservedNotification(user, accommodation, savedReservation);
        return reservationMapper.toDetail(savedReservation);
    }

    @Override
    public ReservationResponseDetail cancelReservation(Long reservationId) {
        Reservation reservation = reservationServiceHelper.getReservationEntityById(reservationId);

        reservationServiceHelper.validateReservationCancellable(reservation);
        reservation.setBookingStatus(BookingStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);

        emailServiceHelper.sendCancellationConfirmationEmail(reservation.getUser(), reservation.getAccommodation(), reservation);
        emailServiceHelper.sendCancellationNotificationToOwnerEmail(reservation.getUser(), reservation.getAccommodation(), reservation);
        return reservationMapper.toDetail(savedReservation);
    }
}