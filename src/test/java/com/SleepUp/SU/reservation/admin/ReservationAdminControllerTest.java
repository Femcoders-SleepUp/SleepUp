package com.SleepUp.SU.reservation.admin;

import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.controller.ReservationController;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.service.ReservationServiceImpl;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String RESERVATIONS_PATH = "/reservations";
    private static final String ACCOMMODATIONS_PATH = "/accommodations";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAdminServiceImpl userAdminServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationServiceImpl reservationServiceImpl;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private AccommodationRepository accommodationRepository;

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
    class DeleteReservationAdminTests {

        @Test
        void deleteReservation_asAdmin_shouldReturnNoContent() throws  Exception {
            Long reservationId = 3L;

            mockMvc.perform(delete(RESERVATIONS_PATH + "/admin/{id}", reservationId)
                            .with(user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteReservation_nonAdmin_shouldReturnForbidden() throws Exception {
            Long reservationId = 3L;

            mockMvc.perform(delete(RESERVATIONS_PATH + "/admin/{id}", reservationId)
                            .with(user(principal)) // usuario normal
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteReservation_notFound_shouldReturnNotFound() throws Exception {
            Long reservationId = 999L;

            mockMvc.perform(delete(RESERVATIONS_PATH + "/admin/{id}", reservationId)
                            .with(user("admin").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Reservation with id '" + reservationId + "' not found"));
        }

    }
}
