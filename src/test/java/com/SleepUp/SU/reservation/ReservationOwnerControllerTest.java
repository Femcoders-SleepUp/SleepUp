package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationOwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationOwnerService reservationOwnerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails principal;
    private Long accommodationId = 123L;

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

    @Test
    void getReservationOnMyAccommodation_authorized_shouldReturnOk() throws Exception {
        List<ReservationResponseSummary> summaries = List.of(
                new ReservationResponseSummary(
                        1L,
                        "alice",
                        2,
                        "Beach House",
                        LocalDate.of(2025, 9, 25),
                        LocalDate.of(2025, 9, 30),
                        BookingStatus.CONFIRMED,
                        true,
                        LocalDateTime.of(2025, 9, 1, 10, 30)
                ),
                new ReservationResponseSummary(
                        2L,
                        "bob",
                        4,
                        "Mountain Cabin",
                        LocalDate.of(2025, 10, 5),
                        LocalDate.of(2025, 10, 12),
                        BookingStatus.PENDING,
                        false,
                        LocalDateTime.of(2025, 9, 15, 14, 0)
                )
        );

        when(reservationOwnerService
                .getAllReservationsOnMyAccommodation(principal.getUser(), accommodationId))
                .thenReturn(summaries);

        mockMvc.perform(get("/api/reservations/accommodation/{id}", accommodationId)
                        .with(user(principal))
                        .param("id", accommodationId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json(objectMapper.writeValueAsString(summaries)));
    }

    @Test
    void getReservationOnMyAccommodation_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reservations/accommodation/{id}", accommodationId)
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }
}
