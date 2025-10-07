package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationOwnerControllerTest {

    private static final String RESERVATION_STATUS_PATH = "/reservations/{id}/status";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails customUserDetailsGuest;
    private CustomUserDetails customUserDetailsOwner;
    private Reservation reservation;

    @BeforeEach
    public void setUp() {
        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        customUserDetailsGuest = new CustomUserDetails(savedUser);

        reservation = reservationRepository.findByUser_Id(savedUser.getId()).getFirst();

        User owner = reservation.getAccommodation().getManagedBy();

        customUserDetailsOwner = new CustomUserDetails(owner);

    }


    @Nested
    class GetAllReservationsForMyAccommodationTest {

        @Test
        void getAllReservations_authorized_shouldReturnList() throws Exception {
            Long accommodationId = reservation.getAccommodation().getId();

            mockMvc.perform(get("/accommodations/{id}/reservations", accommodationId)
                            .with(user(customUserDetailsOwner)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].userName").value("Name4"))
                    .andExpect(jsonPath("$[0].guestNumber").isEmpty())
                    .andExpect(jsonPath("$[0].accommodationName").value("Hotel ABC"))
                    .andExpect(jsonPath("$[0].checkInDate").value("2025-09-21"))
                    .andExpect(jsonPath("$[0].checkOutDate").value("2025-09-24"))
                    .andExpect(jsonPath("$[0].bookingStatus").value("CONFIRMED"))
                    .andExpect(jsonPath("$[0].totalPrice").value(450.00));
        }

        @Test
        void getAllReservations_whenNotOwner_shouldReturnForbidden() throws Exception {
            Long accommodationId = reservation.getAccommodation().getId();

            mockMvc.perform(get("/accommodations/{id}/reservations", accommodationId)
                            .with(user(customUserDetailsGuest)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(containsString("Access denied")));
        }
    }


    @Nested
    class UpdateReservationStatusTest {
        @Test
        void updateReservationStatus_authorized_shouldReturnOk() throws Exception {
            Long id = reservation.getId();
            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CANCELLED);

            ReservationResponseDetail detailDto = new ReservationResponseDetail(
                    id,
                    "alice",
                    2,
                    "Beach House",
                    LocalDate.of(2025, 9, 25),
                    LocalDate.of(2025, 9, 30),
                    BookingStatus.CANCELLED,
                    true,
                    LocalDateTime.of(2025, 9, 1, 10, 30), null
            );

            mockMvc.perform(patch(RESERVATION_STATUS_PATH, id)
                            .with(user(customUserDetailsOwner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.userName").value("Name2"))
                    .andExpect(jsonPath("$.guestNumber").isEmpty())
                    .andExpect(jsonPath("$.accommodationName").value("Hotel ABC"))
                    .andExpect(jsonPath("$.checkInDate").value("2025-10-02"))
                    .andExpect(jsonPath("$.checkOutDate").value("2025-10-06"))
                    .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"))
                    .andExpect(jsonPath("$.emailSent").value(false))
                    .andExpect(jsonPath("$.totalPrice").value(600.00));

        }

        @Test
        void updateReservationStatus_whenNotAccommodationOwner_shouldThrowForbidden() throws Exception {
            Long id = reservation.getId();
            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CANCELLED);

            ReservationResponseDetail detailDto = new ReservationResponseDetail(
                    id,
                    "alice",
                    2,
                    "Beach House",
                    LocalDate.of(2025, 9, 25),
                    LocalDate.of(2025, 9, 30),
                    BookingStatus.CANCELLED,
                    true,
                    LocalDateTime.of(2025, 9, 1, 10, 30), null
            );

            mockMvc.perform(patch(RESERVATION_STATUS_PATH, id)
                            .with(user(customUserDetailsGuest))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(containsString("This reservation does not belong to any of your accommodations.")));

        }

    }
}
