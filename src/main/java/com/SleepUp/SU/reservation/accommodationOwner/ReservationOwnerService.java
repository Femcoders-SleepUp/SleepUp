package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;

import java.util.List;

public interface ReservationOwnerService {

    List<ReservationResponseSummary> getReservationsForMyAccommodation(Long accommodationId);

    ReservationResponseDetail updateStatus(Long id, ReservationAuthRequest reservationAuthRequest);

}
