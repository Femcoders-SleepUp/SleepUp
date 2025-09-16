package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.reservation.status.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponseDetail(
        Long id,
        String userName,
        Integer guestNumber,
        String accommodationName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingStatus bookingStatus,
        Boolean emailSent,
        LocalDateTime createdDate
) {
}
