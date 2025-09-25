package com.SleepUp.SU.reservation.owner;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.EntityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationOwnerServiceImpl {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EntityUtil entityUtil;

    public List<ReservationResponseSummary> getAllReservationsOnMyAccommodation(User user, Long id){
        List<Reservation> allReservationsByAccommodation = reservationRepository.findByAccommodationId(id);
        if (allReservationsByAccommodation.isEmpty()){throw new RuntimeException("Empty list");}
        return entityUtil.mapEntitiesToDTOs(allReservationsByAccommodation,reservationMapper::toSummary);
    }

    @Transactional
    public ReservationResponseDetail updateStatus(Long id, ReservationAuthRequest reservationAuthRequest){
        Reservation isExisting = reservationServiceHelper.getReservationEntityById(id);
        isExisting.setBookingStatus(reservationAuthRequest.bookingStatus());
        return reservationMapper.toDetail(isExisting);
    }

    public ReservationResponseDetail getReservationById(Long id){
        Reservation isExisting = reservationServiceHelper.getReservationEntityById(id);
        return reservationMapper.toDetail(isExisting);
    }

}
