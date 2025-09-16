package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private Accommodation accommodation1;
    private AccommodationRequest accommodationUpdateRequest;
    private Long existingAccommodationId;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("TestUser")
                .orElseThrow(() -> new RuntimeException("TestUser not found"));

        Accommodation newAccommodation = Accommodation.builder()
                .name("Sea View Apartment")
                .price(150.0)
                .guestNumber(4)
                .location("Beach")
                .description("A lovely sea view apartment")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.of(2025, 6, 1))
                .availableTo(LocalDate.of(2025, 12, 31))
                .managedBy(savedUser)
                .imageUrl("image1.jpg")
                .build();

        Accommodation saved = accommodationRepository.save(newAccommodation);

        existingAccommodationId = saved.getId();

        accommodation1 = Accommodation.builder()
                .name("Sea View Apartment")
                .price(150.0)
                .guestNumber(4)
                .location("Beach")
                .description("A lovely sea view apartment")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.of(2025, 6, 1))
                .availableTo(LocalDate.of(2025, 12, 31))
                .managedBy(savedUser)
                .imageUrl("image1.jpg")
                .build();

        accommodationRepository.save(accommodation1);

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

        accommodationUpdateRequest = new AccommodationRequest(
                "Updated Apartment",
                130.0,
                3,
                "New Downtown",
                "Updated description",
                LocalTime.of(15, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 12, 31),
                "updated-image.jpg"
        );
    }

    @Test
    void getAccommodations_shouldReturnListOfAccommodations() throws Exception {
        mockMvc.perform(get("/api/accommodations")
                        .with(user(customUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Apartment"))
                .andExpect(jsonPath("$[0].price").value(120.0))
                .andExpect(jsonPath("$[0].guestNumber").value(3))
                .andExpect(jsonPath("$[0].location").value("Downtown"));
    }

    @Test
    void getAccommodationById_shouldReturnAccommodationDetail() throws Exception {
        mockMvc.perform(get("/api/accommodations/{id}", existingAccommodationId)
                        .with(user(customUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sea View Apartment"))
                .andExpect(jsonPath("$.price").value(150.0))
                .andExpect(jsonPath("$.guestNumber").value(4))
                .andExpect(jsonPath("$.location").value("Beach"))
                .andExpect(jsonPath("$.description").value("A lovely sea view apartment"))
                .andExpect(jsonPath("$.checkInTime").value("14:00:00"))
                .andExpect(jsonPath("$.checkOutTime").value("11:00:00"))
                .andExpect(jsonPath("$.availableFrom").value("2025-06-01"))
                .andExpect(jsonPath("$.availableTo").value("2025-12-31"))
                .andExpect(jsonPath("$.managedByUsername").value("nameTest"));
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

    @Test
    void updateAccommodation_shouldReturnUpdatedAccommodationResponse() throws Exception {
        mockMvc.perform(put("/api/accommodations/{id}", existingAccommodationId)
                        .with(user(customUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accommodationUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Apartment"))
                .andExpect(jsonPath("$.price").value(130.0))
                .andExpect(jsonPath("$.guestNumber").value(3))
                .andExpect(jsonPath("$.location").value("New Downtown"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.checkInTime").value("15:00:00"))
                .andExpect(jsonPath("$.checkOutTime").value("12:00:00"))
                .andExpect(jsonPath("$.availableFrom").value("2025-06-01"))
                .andExpect(jsonPath("$.availableTo").value("2025-12-31"))
                .andExpect(jsonPath("$.imageUrl").value("updated-image.jpg"));
    }
}
