package com.SleepUp.SU.reservation.accommodationOwner;


import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.reservationGuest.ReservationGuestServiceImpl;
import com.SleepUp.SU.reservation.status.BookingStatus;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationOwnerControllerTest {

    private static final String RESERVATIONS_ACCOMMODATION_PATH = "/reservations/accommodation/{id}";
    private static final String RESERVATION_STATUS_PATH = "/reservations/{id}/status";
    private static final String RESERVATION_BY_ID_PATH = "/reservations/{id}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationOwnerService reservationOwnerServiceImpl;

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


//    @Nested
//    class UpdateReservationStatusTest {
//        @Test
//        void updateReservationStatus_authorized_shouldReturnOk() throws Exception {
//            Long id = 42L;
//            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CANCELLED);
//
//            ReservationResponseDetail detailDto = new ReservationResponseDetail(
//                    42L,
//                    "alice",
//                    2,
//                    "Beach House",
//                    LocalDate.of(2025, 9, 25),
//                    LocalDate.of(2025, 9, 30),
//                    BookingStatus.CANCELLED,
//                    true,
//                    LocalDateTime.of(2025, 9, 1, 10, 30)
//            );
//
//            when(reservationOwnerServiceImpl.updateStatus(id, authRequest))
//                    .thenReturn(detailDto);
//
//            mockMvc.perform(patch(RESERVATION_STATUS_PATH, id)
//                            .with(user(principal))
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(authRequest)))
//                    .andExpect(status().isOk())
//                    .andExpect(content().json(objectMapper.writeValueAsString(detailDto)));
//        }
//
//    }

}

