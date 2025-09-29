package com.SleepUp.SU.accommodation.controller;

import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.cloudinary.CloudinaryService;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.accommodation.testUtil.AccommodationTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private CustomUserDetails customUserDetails;
    private CustomUserDetails adminUserDetails;
    private MockMultipartFile imageFileOld;
    private Long existingAccommodationId;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.findByUsername("User2")
                .orElseThrow(() -> new RuntimeException("User2 not found"));

        customUserDetails = new CustomUserDetails(savedUser);

        User adminUser = userRepository.findByUsername("Admin1")
                .orElseThrow(() -> new RuntimeException("Admin1 not found"));

        adminUserDetails = new CustomUserDetails(adminUser);

        imageFileOld = (MockMultipartFile) AccommodationTestData.defaultAccommodationRequestBuilder().image();



        existingAccommodationId = accommodationRepository.findByManagedBy_Id(savedUser.getId())
                .getFirst()
                .getId();
    }

    @Nested
    class GetAccommodations {

        @Test
        void getAccommodations_validUser_shouldReturnListOfAccommodations() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .with(user(customUserDetails))
                            .accept("application/json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Hotel ABC"))
                    .andExpect(jsonPath("$[0].price").value(150.0))
                    .andExpect(jsonPath("$[0].guestNumber").value(2))
                    .andExpect(jsonPath("$[0].petFriendly").value(true))
                    .andExpect(jsonPath("$[0].location").value("New York"));
        }

        @Test
        void getAccommodations_withNoUser_shouldReturnListOfAccommodations() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .accept("application/json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Hotel ABC"))
                    .andExpect(jsonPath("$[0].price").value(150.0))
                    .andExpect(jsonPath("$[0].guestNumber").value(2))
                    .andExpect(jsonPath("$[0].petFriendly").value(true))
                    .andExpect(jsonPath("$[0].location").value("New York"));
        }
    }

    @Nested
    class GetAccommodationById {

        @Test
        void getAccommodationById_validUser_shouldReturnAccommodationResponseDetail() throws Exception {
            mockMvc.perform(get(BASE_API_PATH+ "/{id}", existingAccommodationId)
                            .with(user(customUserDetails))
                            .accept("application/json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Downtown Studio"))
                    .andExpect(jsonPath("$.price").value(220.0))
                    .andExpect(jsonPath("$.guestNumber").value(1))
                    .andExpect(jsonPath("$.petFriendly").value(false))
                    .andExpect(jsonPath("$.location").value("Los Angeles"));
        }

        @Test
        void getAccommodationById_withNoUser_shouldReturnAccommodationResponseDetail() throws Exception {
            mockMvc.perform(get(BASE_API_PATH+ "/{id}", existingAccommodationId)
                            .accept("application/json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Downtown Studio"))
                    .andExpect(jsonPath("$.price").value(220.0))
                    .andExpect(jsonPath("$.guestNumber").value(1))
                    .andExpect(jsonPath("$.petFriendly").value(false))
                    .andExpect(jsonPath("$.location").value("Los Angeles"));
        }

        @Test
        void getAccommodationById_nonExistingId_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/{id}", 999999L)
                            .with(user(adminUserDetails)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateAccommodation {

        @Test
        void createAccommodation_validRequest_shouldReturnAccommodationResponseDetail() throws Exception {
            var request = AccommodationTestData.defaultAccommodationRequestBuilder();

            when(cloudinaryService.uploadFile(any(), anyString()))
                    .thenReturn(Map.of("secure_url", "http://example.com/updated-image.jpg"));

            mockMvc.perform(multipart(BASE_API_PATH )
                            .file(imageFileOld)
                            .param("name", request.name())
                            .param("price", request.price().toString())
                            .param("guestNumber", Integer.toString(request.guestNumber()))
                            .param("petFriendly", Boolean.toString(request.petFriendly()))
                            .param("location", request.location())
                            .param("description", request.description())
                            .param("checkInTime", request.checkInTime().toString())
                            .param("checkOutTime", request.checkOutTime().toString())
                            .param("availableFrom", request.availableFrom().toString())
                            .param("availableTo", request.availableTo().toString())
                            .with(user(customUserDetails))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.name").value(request.name()))
                    .andExpect(jsonPath("$.price").value(request.price()))
                    .andExpect(jsonPath("$.guestNumber").value(request.guestNumber()))
                    .andExpect(jsonPath("$.petFriendly").value(request.petFriendly()))
                    .andExpect(jsonPath("$.location").value(request.location()))
                    .andExpect(jsonPath("$.description").value(request.description()))
                    .andExpect(jsonPath("$.checkInTime").value(request.checkInTime().toString()))
                    .andExpect(jsonPath("$.checkOutTime").value(request.checkOutTime().toString()))
                    .andExpect(jsonPath("$.availableFrom").value(request.availableFrom().toString()))
                    .andExpect(jsonPath("$.availableTo").value(request.availableTo().toString()));
        }

        @Test
        void createAccommodation_missingRequiredFields_shouldReturnBadRequest() throws Exception {
            mockMvc.perform(multipart(BASE_API_PATH)
                            .file(imageFileOld)
                            .param("name", "")  // Empty name
                            .param("price", "-10")  // Invalid price (edge case)
                            .with(user(customUserDetails))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class UpdateAccommodation {

        @Test
        void updateAccommodation_validRequest_shouldReturnUpdatedAccommodationResponse() throws Exception {
            var updateRequest = AccommodationTestData.defaultUpdateRequestBuilder();

            when(cloudinaryService.uploadFile(any(), anyString()))
                    .thenReturn(Map.of("secure_url", "http://example.com/updated-image.jpg"));

            mockMvc.perform(multipart(BASE_API_PATH + "/{id}", existingAccommodationId)
                            .file((MockMultipartFile) AccommodationTestData.defaultAccommodationRequestBuilder().image())
                            .param("name", updateRequest.name())
                            .param("price", updateRequest.price().toString())
                            .param("guestNumber", Integer.toString(updateRequest.guestNumber()))
                            .param("petFriendly", Boolean.toString(updateRequest.petFriendly()))
                            .param("location", updateRequest.location())
                            .param("description", updateRequest.description())
                            .param("checkInTime", updateRequest.checkInTime().toString())
                            .param("checkOutTime", updateRequest.checkOutTime().toString())
                            .param("availableFrom", updateRequest.availableFrom().toString())
                            .param("availableTo", updateRequest.availableTo().toString())
                            .with(user(customUserDetails))
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.name").value(updateRequest.name()))
                    .andExpect(jsonPath("$.price").value(updateRequest.price()))
                    .andExpect(jsonPath("$.guestNumber").value(updateRequest.guestNumber()))
                    .andExpect(jsonPath("$.petFriendly").value(updateRequest.petFriendly()))
                    .andExpect(jsonPath("$.location").value(updateRequest.location()))
                    .andExpect(jsonPath("$.description").value(updateRequest.description()))
                    .andExpect(jsonPath("$.checkInTime").value(updateRequest.checkInTime().toString()))
                    .andExpect(jsonPath("$.checkOutTime").value(updateRequest.checkOutTime().toString()))
                    .andExpect(jsonPath("$.availableFrom").value(updateRequest.availableFrom().toString()))
                    .andExpect(jsonPath("$.availableTo").value(updateRequest.availableTo().toString()))
                    .andExpect(jsonPath("$.imageUrl").value("http://example.com/updated-image.jpg"));
        }

        @Test
        void updateAccommodation_nonExistingId_shouldReturnNotFound() throws Exception {
            var updateRequest = AccommodationTestData.defaultUpdateRequestBuilder();

            MockMultipartFile dummyImageFile = (MockMultipartFile) AccommodationTestData.defaultAccommodationRequestBuilder().image();

            mockMvc.perform(multipart(BASE_API_PATH + "/{id}", 999999L)
                            .file(dummyImageFile)
                            .param("name", updateRequest.name())
                            .param("price", updateRequest.price().toString())
                            .param("guestNumber", Integer.toString(updateRequest.guestNumber()))
                            .param("petFriendly", Boolean.toString(updateRequest.petFriendly()))
                            .param("location", updateRequest.location())
                            .param("description", updateRequest.description())
                            .param("checkInTime", updateRequest.checkInTime().toString())
                            .param("checkOutTime", updateRequest.checkOutTime().toString())
                            .param("availableFrom", updateRequest.availableFrom().toString())
                            .param("availableTo", updateRequest.availableTo().toString())
                            .with(user(adminUserDetails))
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            }))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    class DeleteAccommodation {

        @Test
        void deleteAccommodation_existingId_shouldReturnNoContent() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/{id}", existingAccommodationId)
                            .with(user(customUserDetails)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteAccommodation_userWithoutPermission_nonExistingId_shouldReturnForbidden() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/{id}", 999999L)
                            .with(user(customUserDetails)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void deleteAccommodation_admin_nonExistingId_shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/{id}", 999999L)
                            .with(user(adminUserDetails))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

    }

}
