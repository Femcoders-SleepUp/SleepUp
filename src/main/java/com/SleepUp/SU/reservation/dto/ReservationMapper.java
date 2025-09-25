package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "accommodationName", source = "accommodation.name")
    ReservationResponseSummary toSummary(Reservation reservation);

    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "accommodationName", source = "accommodation.name")
    ReservationResponseDetail toDetail(Reservation reservation);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "accommodation", source = "accommodation")
    @Mapping(target = "guestNumber", source = "reservationRequest.guestNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailSent", source = "emailSent")
    Reservation toEntity(ReservationRequest reservationRequest, BookingStatus bookingStatus, User user, Accommodation accommodation, Boolean emailSent);
}
