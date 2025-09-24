package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.common.AccommodationService;
import com.SleepUp.SU.accommodation.common.AccommodationServiceHelper;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.utils.EmailServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EmailServiceHelper emailServiceHelper;
    private final UserRepository userRepository;
    private final AccommodationServiceHelper accommodationServiceHelper;


    public ReservationResponseDetail createReservation(ReservationRequest reservationRequest, User user, Long accommodationId){
        reservationServiceHelper.validateReservationDates(reservationRequest);

        Accommodation accommodation = accommodationServiceHelper.getAccommodationEntityById(accommodationId);

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
    public List<ReservationResponseSummary> getMyReservations(String username){

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User " + username + " not found"));


        List<Reservation> reservations = reservationRepository.findByUser(user);
        return reservations.stream()
                .map(reservation -> reservationMapper.toSummary(reservation))
                .toList();
    }

    public List<ReservationResponseSummary> getMyFutureReservations(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User " + username + " not found"));

        List<Reservation> reservations = reservationRepository.findByUser(user);

        LocalDate today = LocalDate.now();

        return reservations.stream()
                .filter(reservation -> reservation.getCheckInDate().isAfter(today))
                .map(reservation -> reservationMapper.toSummary(reservation))
                .toList();
    }

    public List<ReservationResponseSummary> getMyHistoryReservations(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User " + username + " not found"));
        List<Reservation> reservations = reservationRepository.findByUser(user);

        LocalDate today = LocalDate.now();

        return reservations.stream()
                .filter(reservation -> reservation.getCheckOutDate().isBefore(today))
                .map(reservation -> reservationMapper.toSummary(reservation))
                .toList();
    }

    public ReservationResponseDetail cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationServiceHelper.findReservationByIdAndUser(reservationId, userId);

        reservationServiceHelper.validateReservationCancellable(reservation);
        reservation.setBookingStatus(BookingStatus.CANCELLED);
        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toDetail(savedReservation);
    }
}