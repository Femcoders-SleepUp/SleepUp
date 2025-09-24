package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping("/accommodation/{accommodationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDetail createReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ReservationRequest reservationRequest,
            @PathVariable Long accommodationId
    ) {
        return reservationService.createReservation(reservationRequest, customUserDetails.getUser(), accommodationId);
    }

    @GetMapping("/all/{user}")
    public ResponseEntity<List<ReservationResponseSummary>> getMyReservations(Principal principal) {
        String username = principal.getName();
        List<ReservationResponseSummary> reservations = reservationService.getMyReservations(username);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/future/{user}")
    public ResponseEntity<List<ReservationResponseSummary>> getMyFutureReservations(Principal principal) {
        String username = principal.getName();
        List<ReservationResponseSummary> futureReservations = reservationService.getMyReservations(username);
        return ResponseEntity.ok(futureReservations);
    }

    @GetMapping("/history/{user}")
    public ResponseEntity<List<ReservationResponseSummary>> getMyPastReservations(Principal principal) {
        String username = principal.getName();
        List<ReservationResponseSummary> pastReservations = reservationService.getMyReservations(username);
        return ResponseEntity.ok(pastReservations);
    }

    @PatchMapping("cancel/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail cancelReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long id
    ) {
        return reservationService.cancelReservation(id, customUserDetails.getUser().getId());
    }

}