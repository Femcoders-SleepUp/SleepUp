package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/{user}/reservations")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam ReservationTime time
    ) {
        return reservationService.getMyReservations(customUserDetails.getId(), time);
    }

    @PostMapping("/{id}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDetail createReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ReservationRequest reservationRequest,
            @PathVariable Long accommodationId
    ) {
        return reservationService.createReservation(reservationRequest, customUserDetails.getUser(), accommodationId);
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

    @PatchMapping(value = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail cancelReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long id
    ) {
        return reservationService.cancelReservation(id);
    }

}