package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationOwnerController {
    private final ReservationOwnerService reservationOwnerService;

    @PatchMapping("accommodations/{id}/reservations")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getAllReservationsForMyAccommodation(@PathVariable Long id){
        return reservationOwnerService.getReservationsForMyAccommodation(id);
    }

    @PatchMapping("reservations/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail updateReservationStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Long id,
                                                             @RequestBody ReservationAuthRequest reservationAuthRequest){
        return reservationOwnerService.updateStatus(id, reservationAuthRequest);
    }

}
