package com.SleepUp.SU.reservation.admin;


import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationByAdmin(@PathVariable Long id) {
        reservationAdminService.deleteReservationByAdmin(id);
    }

}
