package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail getReservationById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                        @PathVariable Long id){
        return reservationGuestService.getReservationById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageDto updateReservation(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                           @PathVariable Long id,
                                           @RequestBody ReservationRequest reservationRequest){
        return reservationGuestService.updateReservation(id, reservationRequest, customUserDetails.getUser());
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail updateReservationStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Long id,
                                                             @RequestBody ReservationAuthRequest reservationAuthRequest){
        return reservationGuestService.updateStatus(id, reservationAuthRequest);
    }

    @PatchMapping(value = "/accommodations/{accommodationId}/reservations/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageDto cancelReservation(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long accommodationId
    ) {
        return reservationGuestService.cancelReservation(accommodationId);
    }


}
