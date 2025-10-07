package com.SleepUp.SU.reservation.service;

import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.reservationTime.ReservationTime;
import com.SleepUp.SU.user.entity.User;

import java.util.List;

public interface ReservationService {

    List<ReservationResponseSummary> getMyReservations(Long userId, ReservationTime time);

    ReservationResponseDetail createReservation(ReservationRequest reservationRequest, User user, Long accommodationId);
}