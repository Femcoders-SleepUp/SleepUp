package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;

import java.util.List;

public interface ReservationOwnerService {

    List<ReservationResponseSummary> getReservationsForMyAccommodation(Long accommodationId);

}
