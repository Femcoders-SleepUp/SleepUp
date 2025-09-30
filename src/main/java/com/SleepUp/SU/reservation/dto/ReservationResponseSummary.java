package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.reservation.status.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ReservationResponseSummary(
        Long id,
        String userName,
        Integer guestNumber,
        String accommodationName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkInDate,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkOutDate,
        BookingStatus bookingStatus,
        Boolean emailSent,
        LocalDateTime createdDate
) {
}
