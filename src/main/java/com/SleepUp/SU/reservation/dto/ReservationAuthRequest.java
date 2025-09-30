package com.SleepUp.SU.reservation.dto;

import com.SleepUp.SU.reservation.status.BookingStatus;
import lombok.Builder;

@Builder
public record ReservationAuthRequest(
        BookingStatus bookingStatus
) {
}
