package com.SleepUp.SU.reservation.admin;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.service.ReservationServiceImpl;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReservationAdminControllerTest {

    private static final String RESERVATIONS_ADMIN_PATH = "/reservations/admin";
    private static final String RESERVATIONS_ADMIN_PATH_ID = "/reservations/admin/{id}";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAdminServiceImpl userAdminServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationServiceImpl reservationServiceImpl;

    private CustomUserDetails principal;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("User2 not found"));
        principal = new CustomUserDetails(savedUser);
    }

    @Nested
    class GetAllReservationsAdminTests {

        @Test
        void getAllReservations_asAdmin_shouldReturnNoContent() throws  Exception {
            Reservation reservation = reservationRepository.findById(1L).orElseThrow(() -> new RuntimeException("Reservation not found"));

            mockMvc.perform(get(RESERVATIONS_ADMIN_PATH)
                            .with(user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].id").value(reservation.getId()))
                    .andExpect(jsonPath("$[0].userName").value(reservation.getUser().getName()))
                    .andExpect(jsonPath("$[0].guestNumber").value(reservation.getGuestNumber()))
                    .andExpect(jsonPath("$[0].accommodationName").value(reservation.getAccommodation().getName()))
                    .andExpect(jsonPath("$[0].checkInDate").value(reservation.getCheckInDate().toString()))
                    .andExpect(jsonPath("$[0].checkOutDate").value(reservation.getCheckOutDate().toString()))
                    .andExpect(jsonPath("$[0].bookingStatus").value(reservation.getBookingStatus().toString()))
                    .andExpect(jsonPath("$[0].totalPrice").value(reservation.getTotalPrice().doubleValue()));
        }

        @Test
        void getAllReservations_nonAdmin_shouldReturnForbidden() throws Exception {
            mockMvc.perform(get(RESERVATIONS_ADMIN_PATH)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteReservationAdminTests {

        @Test
        void deleteReservation_asAdmin_shouldReturnNoContent() throws  Exception {
            Long reservationId = 3L;

            mockMvc.perform(delete(RESERVATIONS_ADMIN_PATH_ID, reservationId)
                            .with(user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteReservation_nonAdmin_shouldReturnForbidden() throws Exception {
            Long reservationId = 3L;

            mockMvc.perform(delete(RESERVATIONS_ADMIN_PATH_ID, reservationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteReservation_notFound_shouldReturnNotFound() throws Exception {
            Long reservationId = 999L;

            mockMvc.perform(delete(RESERVATIONS_ADMIN_PATH_ID, reservationId)
                            .with(user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Reservation with id '" + reservationId + "' not found"));
        }

    }
}
