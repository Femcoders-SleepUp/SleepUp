package com.SleepUp.SU.accommodation.service;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.accommodation.testUtil.AccommodationTestData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.SleepUp.SU.accommodation.testUtil.AccommodationTestData.defaultAccommodationRequestBuilder;
import static com.SleepUp.SU.accommodation.testUtil.AccommodationTestData.defaultUpdateRequestBuilder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceImplTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private AccommodationServiceHelper accommodationServiceHelper;

    @Mock
    private EntityUtil entityUtil;

    @InjectMocks
    private AccommodationServiceImpl accommodationService;

    private Accommodation accommodation;
    private AccommodationRequest accommodationRequest;
    private AccommodationResponseDetail accommodationResponseDetail;
    private User user;

    @BeforeEach
    void setUp() {
        user = AccommodationTestData.TEST_USER;
        accommodationRequest = defaultAccommodationRequestBuilder();
        accommodation = AccommodationTestData.defaultAccommodation();
        accommodationResponseDetail = AccommodationTestData.defaultAccommodationResponseDetailBuilder();
    }

    @Nested
    class GetAllAccommodationsTests {
        @Test
        void getAllAccommodations_noEntities_shouldReturnEmptyList() {
            when(accommodationRepository.findAll()).thenReturn(List.of());

            List<AccommodationResponseSummary> result = accommodationService.getAllAccommodations();

            assertThat(result).isEmpty();
            verify(accommodationRepository).findAll();
            verifyNoInteractions(accommodationMapper);
        }

        @Test
        void getAllAccommodations_someEntities_shouldReturnMappedList() {
            when(accommodationRepository.findAll()).thenReturn(List.of(accommodation));
            when(accommodationMapper.toSummary(accommodation)).thenReturn(
                    AccommodationTestData.defaultAccommodationResponseSummaryBuilder()
            );

            List<AccommodationResponseSummary> result = accommodationService.getAllAccommodations();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo(accommodation.getName());
            verify(accommodationRepository).findAll();
            verify(accommodationMapper).toSummary(accommodation);
        }
    }

    @Nested
    class GetAccommodationByIdTests {
        @Test
        void getAccommodationById_existingId_shouldReturnDetails() {
            when(accommodationServiceHelper.getAccommodationEntityById(1L)).thenReturn(accommodation);
            when(accommodationMapper.toDetail(accommodation)).thenReturn(accommodationResponseDetail);

            AccommodationResponseDetail result = accommodationService.getAccommodationById(1L);

            assertThat(result).isEqualTo(accommodationResponseDetail);
            verify(accommodationServiceHelper).getAccommodationEntityById(1L);
            verify(accommodationMapper).toDetail(accommodation);
        }

        @Test
        void getAccommodationById_nonExistingId_shouldThrow() {
            Long id = 2L;
            when(accommodationServiceHelper.getAccommodationEntityById(id)).thenThrow(new AccommodationNotFoundByIdException(id));

            assertThatThrownBy(() -> accommodationService.getAccommodationById(id))
                    .isInstanceOf(AccommodationNotFoundByIdException.class)
                    .hasMessageContaining("Accommodation with id '" + id + "' not found");

            verify(accommodationServiceHelper).getAccommodationEntityById(id);
            verifyNoInteractions(accommodationMapper);
        }
    }

    @Nested
    class CreateAccommodationTests {
        @Test
        void createAccommodation_validRequest_shouldSaveAndReturnDetails() {
            doNothing().when(accommodationServiceHelper).validateAccommodationNameDoesNotExist(accommodationRequest.name());
            when(accommodationMapper.toEntity(accommodationRequest, user)).thenReturn(accommodation);
            doNothing().when(accommodationServiceHelper).postImageCloudinary(accommodationRequest, accommodation);
            when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
            when(accommodationMapper.toDetail(accommodation)).thenReturn(accommodationResponseDetail);

            AccommodationResponseDetail result = accommodationService.createAccommodation(accommodationRequest, user);

            verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist(accommodationRequest.name());
            verify(accommodationMapper).toEntity(accommodationRequest, user);
            verify(accommodationServiceHelper).postImageCloudinary(accommodationRequest, accommodation);
            verify(accommodationRepository).save(accommodation);
            verify(accommodationMapper).toDetail(accommodation);

            assertThat(result).isEqualTo(accommodationResponseDetail);
        }

        @Test
        void createAccommodation_nameExists_shouldThrow() {
            doThrow(new IllegalArgumentException("Name exists")).when(accommodationServiceHelper).validateAccommodationNameDoesNotExist(anyString());

            assertThatThrownBy(() -> accommodationService.createAccommodation(accommodationRequest, user))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Name exists");

            verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist(accommodationRequest.name());
            verify(accommodationMapper, never()).toEntity(any(), any());
            verify(accommodationRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateAccommodationTests {
        @Test
        void updateAccommodation_nameChanged_shouldValidateAndUpdate() {
            AccommodationRequest updatedRequest = defaultUpdateRequestBuilder();

            when(accommodationServiceHelper.getAccommodationEntityById(1L)).thenReturn(accommodation);
            doNothing().when(accommodationServiceHelper).validateAccommodationNameDoesNotExist(updatedRequest.name());
            doNothing().when(accommodationServiceHelper).cloudinaryManagement(updatedRequest, accommodation);
            when(accommodationMapper.toDetail(accommodation)).thenReturn(accommodationResponseDetail);

            AccommodationResponseDetail result = accommodationService.updateAccommodation(1L, updatedRequest);

            verify(accommodationServiceHelper).getAccommodationEntityById(1L);
            verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist(updatedRequest.name());
            verify(entityUtil).updateField(eq(updatedRequest.name()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.price()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.petFriendly()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.guestNumber()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.location()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.description()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.checkInTime()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.checkOutTime()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.availableFrom()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.availableTo()), any(), any());
            verify(accommodationServiceHelper).cloudinaryManagement(updatedRequest, accommodation);
            verify(accommodationMapper).toDetail(accommodation);

            assertThat(result).isEqualTo(accommodationResponseDetail);
        }

        @Test
        void updateAccommodation_nameUnchanged_shouldSkipValidation() {
            AccommodationRequest updatedRequest = defaultAccommodationRequestBuilder();

            when(accommodationServiceHelper.getAccommodationEntityById(1L)).thenReturn(accommodation);
            doNothing().when(accommodationServiceHelper).cloudinaryManagement(updatedRequest, accommodation);
            when(accommodationMapper.toDetail(accommodation)).thenReturn(accommodationResponseDetail);

            AccommodationResponseDetail result = accommodationService.updateAccommodation(1L, updatedRequest);

            verify(accommodationServiceHelper).getAccommodationEntityById(1L);
            verify(accommodationServiceHelper, never()).validateAccommodationNameDoesNotExist(anyString());
            verify(entityUtil).updateField(eq(updatedRequest.name()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.price()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.petFriendly()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.guestNumber()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.location()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.description()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.checkInTime()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.checkOutTime()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.availableFrom()), any(), any());
            verify(entityUtil).updateField(eq(updatedRequest.availableTo()), any(), any());
            verify(accommodationServiceHelper).cloudinaryManagement(updatedRequest, accommodation);
            verify(accommodationMapper).toDetail(accommodation);

            assertThat(result).isEqualTo(accommodationResponseDetail);
        }

        @Test
        void updateAccommodation_nameExists_shouldThrow() {
            AccommodationRequest updatedRequest = defaultUpdateRequestBuilder();

            when(accommodationServiceHelper.getAccommodationEntityById(1L)).thenReturn(accommodation);
            doThrow(new AccommodationAlreadyExistsByNameException(updatedRequest.name())).when(accommodationServiceHelper).validateAccommodationNameDoesNotExist(updatedRequest.name());

            assertThatThrownBy(() -> accommodationService.updateAccommodation(1L, updatedRequest))
                    .isInstanceOf(AccommodationAlreadyExistsByNameException.class)
                    .hasMessageContaining( "Accommodation with name '" + updatedRequest.name() + "' already exists");

            verify(accommodationServiceHelper).getAccommodationEntityById(1L);
            verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist(updatedRequest.name());
            verify(entityUtil, never()).updateField(any(), any(), any());
            verify(accommodationMapper, never()).toDetail(any());
        }
    }

    @Nested
    class DeleteAccommodationTests {
        @Test
        void deleteAccommodation_existingId_shouldCallDelete() {
            when(accommodationServiceHelper.getAccommodationEntityById(1L)).thenReturn(accommodation);
            doNothing().when(accommodationServiceHelper).deleteImageCloudinary(accommodation.getImageUrl());
            doNothing().when(accommodationRepository).delete(accommodation);

            accommodationService.deleteAccommodation(1L);

            verify(accommodationServiceHelper).getAccommodationEntityById(1L);
            verify(accommodationServiceHelper).deleteImageCloudinary(accommodation.getImageUrl());
            verify(accommodationRepository).delete(accommodation);
        }

        @Test
        void deleteAccommodation_nonExistingId_shouldThrow() {
            Long id = 2L;
            when(accommodationServiceHelper.getAccommodationEntityById(id)).thenThrow(new AccommodationNotFoundByIdException(id));

            assertThatThrownBy(() -> accommodationService.deleteAccommodation(id))
                    .isInstanceOf(AccommodationNotFoundByIdException.class)
                    .hasMessageContaining("Accommodation with id '" + id + "' not found");

            verify(accommodationServiceHelper).getAccommodationEntityById(id);
            verify(accommodationServiceHelper, never()).deleteImageCloudinary(any());
            verify(accommodationRepository, never()).deleteById(any());
        }
    }
}
