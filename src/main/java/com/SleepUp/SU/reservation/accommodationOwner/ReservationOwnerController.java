package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationOwnerController {
    private final ReservationOwnerService reservationOwnerService;

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail updateReservationStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Long id,
                                                             @RequestBody ReservationAuthRequest reservationAuthRequest){
        return reservationOwnerService.updateStatus(id, reservationAuthRequest);
    }

}
