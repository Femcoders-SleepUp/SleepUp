package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.cloudinary.CloudinaryService;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccommodationControllerIntegrationTest {

    private static final String BASE_API_PATH = "/accommodations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private CustomUserDetails customUserDetails;
    private AccommodationRequest accommodationRequest;
    private Accommodation accommodation1;
    private AccommodationRequest accommodationUpdateRequest;
    private Long existingAccommodationId;
    private Accommodation savedAccommodation;
    private MockMultipartFile imageFileOld;
    private MockMultipartFile imageFileNew;

    @BeforeEach
    void setUp() {

        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        imageFileOld = new MockMultipartFile(
                "image",
                "old-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        imageFileNew = new MockMultipartFile(
                "image",
                "new-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        customUserDetails = new CustomUserDetails(savedUser);

        savedAccommodation = accommodationRepository.findByManagedBy_Id(savedUser.getId()).getFirst();

        existingAccommodationId = savedAccommodation.getId();

        accommodation1 = Accommodation.builder()
                .name("Sea View Apartment")
                .price(150.0)
                .guestNumber(4)
                .petFriendly(true)
                .location("Beach")
                .description("A lovely sea view apartment")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.of(2025, 6, 1))
                .availableTo(LocalDate.of(2025, 12, 31))
                .managedBy(savedUser)
                .imageUrl("http://test-image-url.com/old-image.jpg")
                .build();

        accommodationRepository.save(accommodation1);

        accommodationRequest = new AccommodationRequest(
                "Test Apartment",
                120.0,
                3,
                true,
                "Downtown",
                "A nice place to stay",
                LocalTime.of(14, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 12, 31),
                imageFileOld
        );

        accommodationUpdateRequest = new AccommodationRequest(
                "Updated Apartment",
                130.0,
                3,
                true,
                "New Downtown",
                "Updated description",
                LocalTime.of(15, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 6, 1),
                LocalDate.of(2025, 12, 31),
                imageFileNew
        );
    }

    @Test
    void getAccommodations_shouldReturnListOfAccommodations() throws Exception {
        mockMvc.perform(get(BASE_API_PATH)
                        .with(user(customUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hotel ABC"))
                .andExpect(jsonPath("$[0].price").value(150.0))
                .andExpect(jsonPath("$[0].guestNumber").value(2))
                .andExpect(jsonPath("$[0].petFriendly").value(true))
                .andExpect(jsonPath("$[0].location").value("New York"));
    }

    @Test
    void getAccommodationById_shouldReturnAccommodationDetail() throws Exception {
        mockMvc.perform(get(BASE_API_PATH + "/{id}", accommodation1.getId())
                        .with(user(customUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sea View Apartment"))
                .andExpect(jsonPath("$.price").value(150.0))
                .andExpect(jsonPath("$.guestNumber").value(4))
                .andExpect(jsonPath("$.petFriendly").value(true))
                .andExpect(jsonPath("$.location").value("Beach"))
                .andExpect(jsonPath("$.description").value("A lovely sea view apartment"))
                .andExpect(jsonPath("$.checkInTime").value("14:00:00"))
                .andExpect(jsonPath("$.checkOutTime").value("11:00:00"))
                .andExpect(jsonPath("$.availableFrom").value("2025-06-01"))
                .andExpect(jsonPath("$.availableTo").value("2025-12-31"))
                .andExpect(jsonPath("$.managedByUsername").value("Name2"));
    }

    @Test
    void getAccommodationById_NotExisting_shouldThrow() throws Exception {
        Long nonExistingId = 999L;
        mockMvc.perform(get(BASE_API_PATH + "/{id}", nonExistingId)
                        .with(user(customUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Accommodation with id '" + nonExistingId + "' not found"));
    }

    @Test
    void createAccommodation_shouldReturnAccommodationResponseDetail() throws Exception {
        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(java.util.Map.of("secure_url", "http://example.com/updated-image.jpg"));

        mockMvc.perform(multipart(BASE_API_PATH)
                        .file(imageFileOld)
                        .param("name", "Test Apartment")
                        .param("price", "120.0")
                        .param("guestNumber", "3")
                        .param("petFriendly", "true")
                        .param("location", "Downtown")
                        .param("description", "A nice place to stay")
                        .param("checkInTime", "14:00:00")
                        .param("checkOutTime", "12:00:00")
                        .param("availableFrom", "2025-05-01")
                        .param("availableTo", "2025-12-31")
                        .with(user(customUserDetails)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Apartment"))
                .andExpect(jsonPath("$.price").value(120.0))
                .andExpect(jsonPath("$.guestNumber").value(3))
                .andExpect(jsonPath("$.petFriendly").value(true))
                .andExpect(jsonPath("$.location").value("Downtown"))
                .andExpect(jsonPath("$.description").value("A nice place to stay"))
                .andExpect(jsonPath("$.checkInTime").value("14:00:00"))
                .andExpect(jsonPath("$.checkOutTime").value("12:00:00"))
                .andExpect(jsonPath("$.availableFrom").value("2025-05-01"))
                .andExpect(jsonPath("$.availableTo").value("2025-12-31"))
                .andExpect(jsonPath("$.managedByUsername").value("Name2"));
    }

    @Test
    void updateAccommodation_shouldReturnUpdatedAccommodationResponse() throws Exception {
        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(java.util.Map.of("secure_url", "http://example.com/updated-image.jpg"));

        mockMvc.perform(multipart(BASE_API_PATH + "/{id}", savedAccommodation.getId())
                        .file(imageFileNew)
                        .param("name", "Updated Apartment")
                        .param("price", "130.0")
                        .param("guestNumber", "3")
                        .param("petFriendly", "true")
                        .param("location", "New Downtown")
                        .param("description", "Updated description")
                        .param("checkInTime", "15:00:00")
                        .param("checkOutTime", "12:00:00")
                        .param("availableFrom", "2025-06-01")
                        .param("availableTo", "2025-12-31")
                        .with(user(customUserDetails))
                        .with(request -> { request.setMethod("PUT"); return request; })) // force PUT
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Apartment"))
                .andExpect(jsonPath("$.price").value(130.0))
                .andExpect(jsonPath("$.guestNumber").value(3))
                .andExpect(jsonPath("$.petFriendly").value(true))
                .andExpect(jsonPath("$.location").value("New Downtown"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.checkInTime").value("15:00:00"))
                .andExpect(jsonPath("$.checkOutTime").value("12:00:00"))
                .andExpect(jsonPath("$.availableFrom").value("2025-06-01"))
                .andExpect(jsonPath("$.availableTo").value("2025-12-31"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/updated-image.jpg"));
    }

    @Test
    void deleteAccommodation_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete(BASE_API_PATH + "/{id}", existingAccommodationId)
                        .with(user(customUserDetails)))
                .andExpect(status().isNoContent());
    }
}
