package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    public class ReservationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ReservationService reservationService;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        private CustomUserDetails principal;

        @BeforeEach
        void setUp() {
            User testUser = userRepository.findByUsername("TestUser").orElseGet(() -> {
                User user = new User();
                user.setId(1L);
                user.setUsername("TestUser");
                user.setEmail("testuser@example.com");
                user.setName("Test User");
                user.setRole(Role.USER);
                return userRepository.save(user);
            });

            principal = new CustomUserDetails(testUser);
        }

        @Test
        void when_createReservation_then_return_created_reservation() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            ReservationResponseDetail response = new ReservationResponseDetail(
                    1L,
                    "Test User",
                    2,
                    "Test Hotel",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3),
                    BookingStatus.PENDING,
                    false,
                    LocalDateTime.now()
            );

            Long accommodationId = 1L;

            when(reservationService.createReservation(any(ReservationRequest.class), any(User.class), eq(accommodationId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/reservations/accommodation/1")
                            .with(user(principal))
                            .param("accommodationId", accommodationId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.guestNumber").value(2))
                    .andExpect(jsonPath("$.bookingStatus").value("PENDING"))
                    .andExpect(jsonPath("$.accommodationName").value("Test Hotel"))
                    .andExpect(jsonPath("$.userName").value("Test User"));
        }

        @Test
        void when_createReservation_without_authentication_then_return_unauthorized() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            mockMvc.perform(post("/api/reservations/accommodation/{accommodationId}", 1L)
                            .with(anonymous())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }


        @Test
        void when_createReservation_with_invalid_data_then_return_bad_request() throws Exception {
            String invalidJson = """
                    {
                        "checkInDate": null,
                        "checkOutDate": null,
                        "guestNumber": -1
                    }
                    """;

            mockMvc.perform(post("/api/reservations/accommodation/1")
                            .with(user(principal))
                            .param("accommodationId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void when_createReservation_with_past_dates_then_return_bad_request() throws Exception {

            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(1)
            );

            when(reservationService.createReservation(
                    any(ReservationRequest.class),
                    any(User.class),
                    anyLong()
            )).thenThrow(new IllegalArgumentException("Check-in date cannot be in the past"));

            mockMvc.perform(post("/api/reservations/accommodation/{accommodationId}", 1L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void when_createReservation_with_invalid_date_range_then_return_bad_request() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(1)
            );

            when(reservationService.createReservation(
                    any(ReservationRequest.class),
                    any(User.class),
                    anyLong()
            )).thenThrow(new IllegalArgumentException("Check-in date must be before check-out date"));

            mockMvc.perform(post("/api/reservations/accommodation/{accommodationId}", 1L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }


        @Test
        void when_createReservation_accommodation_not_found_then_return_not_found() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );


            when(reservationService.createReservation(
                    any(ReservationRequest.class),
                    any(User.class),
                    anyLong()
            )).thenThrow(new RuntimeException("Accommodation not found"));

            mockMvc.perform(post("/api/reservations/accommodation/{accommodationId}", 999L)
                    .with(user(principal))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void when_createReservation_with_overlapping_dates_then_return_bad_request() throws Exception {
            ReservationRequest request = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            when(reservationService.createReservation(any(ReservationRequest.class), any(User.class), any(Long.class)))
                    .thenThrow(new IllegalArgumentException("You already have a reservation that overlaps with these dates"));

            mockMvc.perform(post("/api/reservations/accommodation/{accommodationId}", 1L)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

        }

        @Test
        @WithMockUser
        void when_cancelReservation_then_return_cancelled_reservation() throws Exception {
            Long reservationId = 1L;

            ReservationResponseDetail response = new ReservationResponseDetail(
                    1L,
                    "Test User",
                    2,
                    "Test Accommodation",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(10),
                    BookingStatus.CANCELLED,
                    false,
                    LocalDateTime.now()
            );

            when(reservationService.cancelReservation(reservationId, 1L))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/reservations/cancel/{id}", reservationId)
                            .with(user(principal))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"));
        }
    }