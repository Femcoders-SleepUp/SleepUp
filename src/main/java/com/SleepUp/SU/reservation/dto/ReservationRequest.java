package com.SleepUp.SU.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationRequest(
        @Positive
        Integer guestNumber,

        @NotNull
        LocalDate checkInDate,

        @NotNull
        LocalDate checkOutDate
) {
}
