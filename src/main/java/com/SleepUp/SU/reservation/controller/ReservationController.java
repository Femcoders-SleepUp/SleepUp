package com.SleepUp.SU.reservation.controller;

import com.SleepUp.SU.reservation.dto.ApiMessage;
import com.SleepUp.SU.reservation.service.ReservationService;
import com.SleepUp.SU.reservation.reservationtime.ReservationTime;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam ReservationTime time
    ) {
        return reservationService.getMyReservations(customUserDetails.getId(), time);
    }

    @PostMapping("/accommodations/{accommodationId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDetail createReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ReservationRequest reservationRequest,
            @PathVariable Long accommodationId
    ) {
        return reservationService.createReservation(reservationRequest, customUserDetails.getUser(), accommodationId);
    }

    @PatchMapping(value = "/accommodations/{accommodationId}/reservations/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ApiMessage cancelReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long accommodationId
    ) {
        return reservationService.cancelReservation(accommodationId);
    }

}