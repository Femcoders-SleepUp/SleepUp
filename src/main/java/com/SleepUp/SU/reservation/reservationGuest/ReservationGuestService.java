package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.reservation.dto.*;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.dto.ApiMessageDto;

public interface ReservationGuestService {

    ApiMessageDto updateReservation(Long id, ReservationRequest reservationRequest, User user);

    ReservationResponseDetail getReservationById(Long id);

    ApiMessageDto cancelReservation(Long reservationId);
}