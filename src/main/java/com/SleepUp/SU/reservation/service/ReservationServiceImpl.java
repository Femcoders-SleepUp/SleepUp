package com.SleepUp.SU.reservation.service;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.reservation.dto.*;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.reservationTime.ReservationTime;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.EntityUtil;
import jakarta.mail.MessagingException;
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
    private final EmailService emailService;
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
    public ReservationResponseDetail createReservation(ReservationRequest reservationRequest, User user, Long accommodationId) {
        reservationServiceHelper.validateReservationDates(reservationRequest);
        Accommodation accommodation = accommodationServiceHelper.getAccommodationEntityById(accommodationId);

        reservationServiceHelper.validateCreateReservation(accommodation, user, reservationRequest);

        Reservation newReservation =  reservationMapper.toEntity(
                reservationRequest,
                BookingStatus.PENDING,
                user, accommodation,
                false);

        reservationServiceHelper.updatePriceWithDiscountIfDeserved(newReservation, accommodation, user);

        Reservation savedReservation = reservationRepository.save(newReservation);

        emailService.sendOwnerReservedNotification(savedReservation);
        return reservationMapper.toDetail(savedReservation);
    }

}