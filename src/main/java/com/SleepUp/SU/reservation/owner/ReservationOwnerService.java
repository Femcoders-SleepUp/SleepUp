package com.SleepUp.SU.reservation.owner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.User;

import java.util.List;

public interface ReservationOwnerService {
    List<ReservationResponseSummary> getAllReservationsOnMyAccommodation(User user, Long accommodationId);

    ReservationResponseDetail updateStatus(Long id, ReservationAuthRequest reservationAuthRequest);

    ReservationResponseDetail getReservationById(Long id);
}
