package com.SleepUp.SU.reservation.admin;


import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;

import java.util.List;

public interface ReservationAdminService {
    List<ReservationResponseSummary> getAllReservations();
}
