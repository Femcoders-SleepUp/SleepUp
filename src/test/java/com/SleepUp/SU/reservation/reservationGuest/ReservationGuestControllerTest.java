package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReservationGuestControllerTest {

    private static final String RESERVATION_CANCEL_PATH = "/reservations/{id}/cancel";
    private static final String RESERVATION_BY_ID_PATH = "/reservations/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        User testUser = userRepository.findByUsername("TestUser").orElseGet(() -> {
            User u = new User();
            u.setUsername("TestUser");
            u.setEmail("testuser@example.com");
            u.setName("Test User");
            u.setRole(Role.USER);
            return userRepository.save(u);
        });
        principal = new CustomUserDetails(testUser);
    }

    @Nested
    class GetReservationByIdTest {

        @Test
        void getReservationById_authorized_shouldReturnOk() throws Exception {
            Long id = 5L;
            String today = LocalDate.now().toString();

            mockMvc.perform(get(RESERVATION_BY_ID_PATH, id)
                            .with(user(principal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.userName").value("Name1"))
                    .andExpect(jsonPath("$.guestNumber").isEmpty())
                    .andExpect(jsonPath("$.accommodationName").value("Mountain View Cabin"))
                    .andExpect(jsonPath("$.checkInDate").value("2025-11-02"))
                    .andExpect(jsonPath("$.checkOutDate").value("2025-11-08"))
                    .andExpect(jsonPath("$.bookingStatus").value("PENDING"))
                    .andExpect(jsonPath("$.emailSent").value(false))
                    .andExpect(jsonPath("$.createdDate", startsWith(today)));

        }

    }

    @Nested
    class CancelReservationTests {

        @Test
        void cancelReservation_validRequest_shouldReturnCancelledReservation2() throws Exception {
            Long reservationId = 5L;

            mockMvc.perform(patch(RESERVATION_CANCEL_PATH, reservationId)
                            .with(user(principal)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", allOf(containsString("reservation"), containsString("cancelled"))));
        }


        @Test
        void cancelReservation_alreadyStarted_shouldReturnConflict() throws Exception {
            Long reservationId = 1L;

            mockMvc.perform(patch(RESERVATION_CANCEL_PATH, reservationId)
                            .with(user(principal)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot modify a reservation that has already started"));
        }

        @Test
        void cancelReservation_pastDates_shouldReturnConflict() throws Exception {
            Long reservationId = 1L;

            mockMvc.perform(patch(RESERVATION_CANCEL_PATH, reservationId)
                            .with(user(principal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot modify a reservation that has already started"));
        }
    }
}

