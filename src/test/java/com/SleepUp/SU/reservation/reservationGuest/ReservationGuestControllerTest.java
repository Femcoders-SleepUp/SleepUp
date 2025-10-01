package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
    private static final String RESERVATION_UPDATE_PATH = "/reservations/{id}";
    private static final String RESERVATION_BY_ID_PATH = "/reservations/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private CustomUserDetails principal;
    private Long reservationId;

    @BeforeEach
    void setUp() {
        User testUser = userRepository.findByUsername("User1")
                .orElseThrow(() -> new RuntimeException("User1 not found"));

        principal = new CustomUserDetails(testUser);

        reservationId = reservationRepository.findByUser_Id(testUser.getId()).getFirst().getId();
    }

    @Nested
    class GetReservationByIdTest {

        @Test
        void getReservationById_authorized_shouldReturnOk() throws Exception {

            mockMvc.perform(get(RESERVATION_BY_ID_PATH, reservationId)
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
                    .andExpect(jsonPath("$.emailSent").value(false));
        }

    }

    @Nested
    class UpdateReservationByIdTest {


        @Test
        void updateReservation_validRequest_shouldReturnUpdatedMessage() throws Exception {

            ReservationRequest updateRequest = ReservationRequest.builder()
                    .guestNumber(3)
                    .checkInDate(LocalDate.of(2025, 11, 2))
                    .checkOutDate(LocalDate.of(2025, 11, 8))
                    .build();

            mockMvc.perform(put(RESERVATION_UPDATE_PATH, reservationId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", allOf(
                            containsString("updated"),
                            containsString("reservation")
                    )));
        }
    }

    @Nested
    class CancelReservationTests {

        @Test
        void cancelReservation_validRequest_shouldReturnCancelledReservation2() throws Exception {

            mockMvc.perform(patch(RESERVATION_CANCEL_PATH, reservationId)
                            .with(user(principal)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", allOf(containsString("reservation"), containsString("cancelled"))));
        }

//
//        @Test
//        void cancelReservation_alreadyStarted_shouldReturnConflict() throws Exception {
//
//            mockMvc.perform(patch(RESERVATION_CANCEL_PATH, reservationId)
//                            .with(user(principal)))
//                    .andExpect(status().isConflict())
//                    .andExpect(jsonPath("$.message").value("Cannot modify a reservation that has already started"));
//        }
//
//        @Test
//        void cancelReservation_pastDates_shouldReturnConflict() throws Exception {
//
//            mockMvc.perform(patch(RESERVATION_CANCEL_PATH, reservationId)
//                            .with(user(principal))
//                            .accept(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isConflict())
//                    .andExpect(jsonPath("$.message").value("Cannot modify a reservation that has already started"));
//        }
    }
}

