package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.controller.ReservationController;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.service.ReservationServiceImpl;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.admin.UserAdminServiceImpl;
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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReservationControllerTest {

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
    class GetReservationsTests {

        @Test
        void getMyReservations_withAllTimeAuthenticated_shouldReturnReservationSummaries() throws Exception {
            mockMvc.perform(get(RESERVATIONS_PATH)
                            .param("time", "ALL")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getMyReservations_unauthenticated_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get(RESERVATIONS_PATH)
                            .param("time", "ALL")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateReservationTests {

        @Test
        void createReservation_validData_shouldReturnCreatedReservation() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    1,
                    LocalDate.of(2025, 11, 1),
                    LocalDate.of(2025, 11, 8)
            );

            Long accommodationId = 2L;
            Accommodation accommodation = accommodationRepository.findById(accommodationId)
                    .orElseThrow(() -> new RuntimeException("Accommodation with id 2L not found"));

            mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", accommodationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accommodationName").value(accommodation.getName()))
                    .andExpect(jsonPath("$.guestNumber").value(1))
                    .andExpect(jsonPath("$.bookingStatus").value("PENDING"))
                    .andExpect(jsonPath("$.userName").value(principal.getUser().getName()));
        }

        @Test
        void createReservation_tooManyGuests_shouldReturnBadRequest() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.of(2025, 10, 15),
                    LocalDate.of(2025, 10, 18)
            );

            mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", 2L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Accommodation supports maximum 1 guests, but 2 guests requested"));
        }

        @Test
        void createReservation_dateOverlap_shouldReturnConflict() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    1,
                    LocalDate.of(2025, 10, 1),
                    LocalDate.of(2025, 10, 8)
            );

            mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", 2L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("You already have a reservation that overlaps with these dates: Reservation at Hotel ABC from 2025-10-02 to 2025-10-06; Reservation at Beachside Bungalow from 2025-10-06 to 2025-10-12"));
        }

        @Test
        void createReservation_notAvailableDates_shouldReturnBadRequest() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            Long accommodationId = 1L;

            mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", accommodationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Accommodation is only available from 2025-09-20 to 2025-09-25"));
        }

        @Test
        void createReservation_unauthenticated_shouldReturnUnauthorized() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            mockMvc.perform(post(RESERVATIONS_PATH + "/accommodation/{accommodationId}", 1L)
                            .with(anonymous())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void createReservation_invalidData_shouldReturnBadRequest() throws Exception {
            String invalidJson = """
                    {
                        "checkInDate": null,
                        "checkOutDate": null,
                        "guestNumber": -1
                    }
                    """;

            mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", 1L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createReservation_pastDates_shouldReturnBadRequest() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(1)
            );

            mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", 1L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class CancelReservationTests {

        @Test
        void cancelReservation_validRequest_shouldReturnCancelledReservation() throws Exception {
            Long reservationId = 5L;

            mockMvc.perform(patch(ACCOMMODATIONS_PATH + "/{id}/reservations/cancel", reservationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        void cancelReservation_alreadyStarted_shouldReturnConflict() throws Exception {
            Long reservationId = 1L;

            mockMvc.perform(patch(ACCOMMODATIONS_PATH + "/{id}/reservations/cancel", reservationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot modify a reservation that has already started"));
        }

        @Test
        void cancelReservation_pastDates_shouldReturnConflict() throws Exception {
            Long reservationId = 1L;

            mockMvc.perform(patch(ACCOMMODATIONS_PATH + "/{id}/reservations/cancel", reservationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot modify a reservation that has already started"));
        }
    }
}
