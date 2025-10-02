package com.SleepUp.SU.reservation.controller;

import com.SleepUp.SU.reservation.service.ReservationService;
import com.SleepUp.SU.reservation.reservationTime.ReservationTime;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservation", description = "Operations related to reservations")
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/reservations")
    @Operation(summary = "Get My Reservations", description = "Retrieve a list of reservations made by the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of reservations"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam ReservationTime time
    ) {
        return reservationService.getMyReservations(customUserDetails.getId(), time);
    }

    @PostMapping("/accommodations/{accommodationId}/reservations")
    @Operation(summary = "Create Reservation", description = "Create a new reservation for a specific accommodation.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created a new reservation"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDetail createReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ReservationRequest reservationRequest,
            @PathVariable Long accommodationId
    ) {
        return reservationService.createReservation(reservationRequest, customUserDetails.getUser(), accommodationId);
    }
}