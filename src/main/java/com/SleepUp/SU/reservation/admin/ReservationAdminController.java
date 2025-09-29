package com.SleepUp.SU.reservation.admin;


import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reservations/admin")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final ReservationAdminServiceImpl reservationAdminService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseSummary> getAllReservations(@AuthenticationPrincipal Reservation reservation){
        return reservationAdminService.getAllReservations();
    }
}
