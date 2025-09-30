package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationOwnerImpl implements ReservationOwnerService{

    private final ReservationRepository reservationRepository;
    private final EntityUtil entityUtil;
    private final ReservationMapper reservationMapper;

    @Override
    public List<ReservationResponseSummary> getReservationsForMyAccommodation(Long accommodationId) {
        List<Reservation> reservations = reservationRepository.findByAccommodationId(accommodationId);
        return entityUtil.mapEntitiesToDTOs(reservations, reservationMapper::toSummary);
    }
}
