package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.reservation.status.BookingStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ReservationResponseSummary(
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
