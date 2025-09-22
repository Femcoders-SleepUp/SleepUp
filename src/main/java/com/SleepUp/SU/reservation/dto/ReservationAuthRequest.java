package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.reservation.status.BookingStatus;

public record ReservationAuthRequest(
        BookingStatus bookingStatus
) {
}
