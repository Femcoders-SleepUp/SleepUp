package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.reservation.Reservation;
import com.SleepUp.SU.reservation.status.BookingStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationResponseSummary toSummary(Reservation reservation);
    ReservationResponseDetail toDetail(Reservation reservation);
    Reservation toEntity(ReservationRequest reservationRequest, BookingStatus bookingStatus);
}
