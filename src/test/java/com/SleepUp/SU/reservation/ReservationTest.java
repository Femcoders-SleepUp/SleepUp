package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
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
    private  ReservationService reservationService;

    @Autowired
    private  ReservationController reservationController;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByUserId() {
        Long userId = 2L; // User2 as per your sample data

        List<Reservation> reservations = reservationRepository.findByUser_Id(userId);

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        System.out.println(reservations);
        // Additional assertions about data content, IDs, or count can be added
    }

    @Test
    public void testFindByUserId_Service() {
        Long userId = 2L; // User2 as per your sample data

        List<ReservationResponseSummary> reservations = reservationService.getMyReservations(userId, ReservationTime.ALL);

        assertNotNull(reservations);
        assertFalse(reservations.isEmpty());
        System.out.println(reservations);
        // Additional assertions about data content, IDs, or count can be added
    }

    @Test
    public void testController(){
        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("TestUser not found"));
        CustomUserDetails principal = new CustomUserDetails(savedUser);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);
        List<ReservationResponseSummary> results = reservationController.getMyReservations(principal, ReservationTime.ALL);
        System.out.println(results.size()); // Expect > 0
        System.out.println(results);
    }
}

