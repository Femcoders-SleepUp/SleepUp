package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
     private final ReservationService reservationService;

     @PostMapping("/accommodation/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDetail createReservation(
             @AuthenticationPrincipal CustomUserDetails customUserDetails,
             @Valid @RequestBody ReservationRequest reservationRequest,
             @RequestParam Long accommodationId
             ){
         return reservationService.createReservation(reservationRequest, customUserDetails.getUser(), accommodationId);
     }
}
