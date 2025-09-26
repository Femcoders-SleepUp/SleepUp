package com.SleepUp.SU.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationRequest(
        @Positive
        Integer guestNumber,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkInDate,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkOutDate
) {
}
