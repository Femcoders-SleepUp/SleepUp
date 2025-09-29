package com.SleepUp.SU.reservation.admin;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations/admin")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final ReservationAdminServiceImpl reservationAdminService;
}
