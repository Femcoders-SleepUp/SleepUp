package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.controller.ReservationController;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.reservationTime.ReservationTime;
import com.SleepUp.SU.reservation.service.ReservationServiceImpl;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReservationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationServiceImpl reservationServiceImpl;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void repository_findByUserId_shouldReturnNonEmptyReservationList() {
        Long userId = 2L;

        List<Reservation> reservations = reservationRepository.findByUser_Id(userId);

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        System.out.println(reservations);
    }

    @Test
    public void service_getMyReservationsAllTime_shouldReturnNonEmptySummaryList() {
        Long userId = 2L;

        List<ReservationResponseSummary> reservations = reservationServiceImpl.getMyReservations(userId, ReservationTime.ALL);

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        System.out.println(reservations);
    }

    @Test
    public void controller_getMyReservationsAllTime_shouldReturnNonEmptySummaryList(){
        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("TestUser not found"));
        CustomUserDetails principal = new CustomUserDetails(savedUser);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);
        List<ReservationResponseSummary> results = reservationController.getMyReservations(principal, ReservationTime.ALL);
        System.out.println(results.size());
        System.out.println(results);
    }
}