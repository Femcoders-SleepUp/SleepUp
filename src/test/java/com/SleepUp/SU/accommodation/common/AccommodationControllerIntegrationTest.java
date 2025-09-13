package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccommodationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails customUserDetails;
    private AccommodationRequest accommodationRequest;

    @BeforeEach
    void setUp() {

        User userUnsaved = User.builder()
                .username("Newuser")
                .name("nameTest")
                .email("usertnest@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(userUnsaved);

        customUserDetails = new CustomUserDetails(savedUser);

        accommodationRequest = new AccommodationRequest(
                "Test Apartment",
                120.0,
                3,
                "Downtown",
                "A nice place to stay",
                LocalTime.of(14, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 12, 31),
                "image.jpg"
        );
    }

    @Test
    void createAccommodation_shouldReturnAccommodationResponseDetail() throws Exception {
        mockMvc.perform(post("/api/accommodations")
                        .with(user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accommodationRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Apartment"))
                .andExpect(jsonPath("$.price").value(120.0))
                .andExpect(jsonPath("$.guestNumber").value(3))
                .andExpect(jsonPath("$.location").value("Downtown"))
                .andExpect(jsonPath("$.description").value("A nice place to stay"))
                .andExpect(jsonPath("$.checkInTime").value("14:00:00"))
                .andExpect(jsonPath("$.checkOutTime").value("12:00:00"))
                .andExpect(jsonPath("$.availableFrom").value("2025-05-01"))
                .andExpect(jsonPath("$.availableTo").value("2025-12-31"))
                .andExpect(jsonPath("$.managedByUsername").value("nameTest"));
    }
}
