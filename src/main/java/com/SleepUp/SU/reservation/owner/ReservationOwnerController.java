package com.SleepUp.SU.reservation.owner;

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
@RequestMapping("api/reservations")
@RequiredArgsConstructor
public class ReservationOwnerController {

    private final ReservationOwnerServiceImpl reservationOwnerServiceImpl;

    @GetMapping("/accommodation/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getReservationOnMyAccommodation(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                            @PathVariable Long id){
        return reservationOwnerServiceImpl.getAllReservationsOnMyAccommodation(customUserDetails.getUser(), id);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail updateReservationStatus(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Long id,
                                                             @RequestBody ReservationAuthRequest reservationAuthRequest){
        return reservationOwnerServiceImpl.updateStatus(id, reservationAuthRequest);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponseDetail getReservationById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Long id){
        return reservationOwnerServiceImpl.getReservationById(id);
    }
}
