package com.SleepUp.SU.reservation.admin;

import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Reservation", description = "Operations related to reservations by admin")
@RestController
@RequestMapping("/reservations/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ReservationAdminController {

    private final ReservationAdminServiceImpl reservationAdminService;

    @GetMapping
    @Operation(summary = "Get All Reservations", description = "Retrieve all reservations in the system. Accessible only by admin users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of all reservations"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getAllReservations(@AuthenticationPrincipal Reservation reservation){
        return reservationAdminService.getAllReservations();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Reservation", description = "Delete a reservation by its ID. Accessible only by admin users.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted the reservation"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/ReservationFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationByAdmin(@PathVariable Long id) {
        reservationAdminService.deleteReservationByAdmin(id);
    }

}