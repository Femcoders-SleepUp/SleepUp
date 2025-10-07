package com.SleepUp.SU.reservation.admin;

import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.ReservationNotFoundByIdException;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationAdminServiceImpl implements ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EntityUtil entityUtil;

    @Override
    public List<ReservationResponseSummary> getAllReservations(){
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(reservationMapper::toSummary).toList();
    }

    @Override
    public void deleteReservationByAdmin(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new ReservationNotFoundByIdException(reservationId);
        }
        reservationRepository.deleteById(reservationId);
    }
}