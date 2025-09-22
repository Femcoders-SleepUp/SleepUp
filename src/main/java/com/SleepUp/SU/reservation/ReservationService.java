package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.utils.EmailServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AccommodationRepository accommodationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EmailServiceHelper emailServiceHelper;


    public ReservationResponseDetail createReservation(ReservationRequest reservationRequest, User user, Long accommodationId){
        reservationServiceHelper.validateReservationDates(reservationRequest);

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("Accommodation not found"));

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

}
