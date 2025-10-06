package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accommodation Owner Reservation", description = "Operations related to reservations for accommodation owners")
@RestController
@RequiredArgsConstructor
public class ReservationOwnerController {
    private final ReservationOwnerService reservationOwnerService;

    @PreAuthorize("hasRole('ADMIN') or @accommodationAccessEvaluator.isOwner(#id, principal.id)")
    @GetMapping("/accommodations/{id}/reservations")
    @Operation(summary = "Get all reservations for my accommodation", description = "Retrieve all reservations for accommodations owned by the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getAllReservationsForMyAccommodation(@PathVariable Long id){
        return reservationOwnerService.getReservationsForMyAccommodation(id);
    }

    @PreAuthorize("hasRole('ADMIN') or @reservationAccessEvaluator.isReservationAccommodationOwner(#id, principal.id)")
    @PatchMapping("/reservations/{id}/status")
    @Operation(summary = "Update Reservation Status", description = "Update the status of an existing reservation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated reservation status"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail updateReservationStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Long id,
                                                             @RequestBody ReservationAuthRequest reservationAuthRequest){
        return reservationOwnerService.updateStatus(id, reservationAuthRequest);
    }

}