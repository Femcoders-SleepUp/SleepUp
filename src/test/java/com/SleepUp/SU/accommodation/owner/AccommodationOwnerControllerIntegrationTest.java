package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccommodationOwnerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private AccommodationOwnerService accommodationOwnerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails customUserDetails;
    private  List<AccommodationResponseSummary> summaries;
    private Accommodation accommodation1;
    private Accommodation accommodation2;

    @BeforeEach
    void setUp() {
        accommodationRepository.deleteAll();

        User savedUser = userRepository.findByUsername("TestUser").get();

        customUserDetails = new CustomUserDetails(savedUser);

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

        accommodation2 = Accommodation.builder()
                .name("Mountain Cabin")
                .price(200.0)
                .guestNumber(6)
                .location("Mountains")
                .description("Cozy cabin in the mountains")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(10, 0))
                .availableFrom(LocalDate.of(2025, 7, 1))
                .availableTo(LocalDate.of(2025, 11, 30))
                .managedBy(savedUser)
                .imageUrl("image2.jpg")
                .build();

        accommodationRepository.saveAll(List.of(accommodation1, accommodation2));
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"USER"})
    void shouldReturnAllOwnerAccommodations() throws Exception {

        mockMvc.perform(get("/api/accommodations/my-user")
                        .with(user(customUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].name").value("Sea View Apartment"))
                .andExpect(jsonPath("$[0].price").value(150.0))
                .andExpect(jsonPath("$[0].guestNumber").value(4))
                .andExpect(jsonPath("$[0].location").value("Beach"))
                .andExpect(jsonPath("$[0].imageUrl").value("image1.jpg"))

                .andExpect(jsonPath("$[1].name").value("Mountain Cabin"))
                .andExpect(jsonPath("$[1].price").value(200.0))
                .andExpect(jsonPath("$[1].guestNumber").value(6))
                .andExpect(jsonPath("$[1].location").value("Mountains"))
                .andExpect(jsonPath("$[1].imageUrl").value("image2.jpg"));
    }
}
