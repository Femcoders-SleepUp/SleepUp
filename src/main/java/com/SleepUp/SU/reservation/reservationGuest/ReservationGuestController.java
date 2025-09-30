package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservation Guest", description = "Operations related to reservations by guests")
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationGuestController {

    private final ReservationGuestService reservationGuestService;

//    @GetMapping("/accommodation/{id}")
//    @ResponseStatus(HttpStatus.OK)
//    public List<ReservationResponseSummary> getReservationOnMyAccommodation(@AuthenticationPrincipal CustomUserDetails customUserDetails,
//                                                                            @PathVariable Long id){
//        return reservationGuestService.getAllReservationsOnMyAccommodation(customUserDetails.getUser(), id);
//    }

    @PostMapping("/{id}")
    @Operation(summary = "Get Reservation by ID", description = "Retrieve detailed information about a specific reservation by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved reservation details"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail getReservationById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                        @PathVariable Long id){
        return reservationGuestService.getReservationById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Reservation", description = "Update the details of an existing reservation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated reservation"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageDto updateReservation(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                           @PathVariable Long id,
                                           @RequestBody ReservationRequest reservationRequest){
        return reservationGuestService.updateReservation(id, reservationRequest, customUserDetails.getUser());
    }

    @PatchMapping("/{id}/status")
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
        return reservationGuestService.updateStatus(id, reservationAuthRequest);
    }

    @PatchMapping(value = "/accommodations/{accommodationId}/reservations/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cancel Reservation", description = "Cancel an existing reservation for a specific accommodation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully canceled the reservation"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageDto cancelReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long accommodationId
    ) {
        return reservationGuestService.cancelReservation(accommodationId);
    }

}