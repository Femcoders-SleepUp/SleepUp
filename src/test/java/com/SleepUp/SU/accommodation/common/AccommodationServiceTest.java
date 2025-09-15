package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.utils.EntityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private EntityUtil entityUtil;

    @Mock
    private AccommodationServiceHelper accommodationServiceHelper;

    @Mock
    private AccommodationService accommodationServiceMock;

    @InjectMocks
    private AccommodationService accommodationService;

    private AccommodationRequest oldAccommodationRequest;
    private AccommodationRequest newAccommodationRequest;
    private Accommodation existingAccommodation;
    private Accommodation updatedAccommodation;
    private AccommodationResponseDetail updatedAccommodationResponseDetail;
    private User user;

    @BeforeEach
    void setUp() {

        oldAccommodationRequest = new AccommodationRequest(
                "Old Name",
                100.0,
                2,
                "Old Location",
                "Old Description",
                LocalTime.of(14, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                "old-image.jpg"
        );

        newAccommodationRequest = new AccommodationRequest(
                "New Name",
                150.0,
                3,
                "New Location",
                "New Description",
                LocalTime.of(15, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 9, 5),
                LocalDate.of(2025, 10, 5),
                "new-image.jpg"
        );

        existingAccommodation = Accommodation.builder()
                .id(1L)
                .name("Old Name")
                .price(100.0)
                .guestNumber(2)
                .location("Old Location")
                .description("Old Description")
                .imageUrl("old-image.jpg")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(12, 0))
                .availableFrom(LocalDate.of(2025, 1, 1))
                .availableTo(LocalDate.of(2025, 12, 31))
                .build();

        updatedAccommodation = Accommodation.builder()
                .id(1L)
                .name("New Name")
                .price(150.0)
                .guestNumber(3)
                .location("New Location")
                .description("New Description")
                .imageUrl("new-image.jpg")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(12, 0))
                .availableFrom(LocalDate.of(2025, 9, 5))
                .availableTo(LocalDate.of(2025, 10, 5))
                .build();

        updatedAccommodationResponseDetail = new AccommodationResponseDetail(
                "New Name",
                150.0,
                3,
                "New Location",
                "New Description",
                "15:00",
                "12:00",
                "2025-09-05",
                "2025-10-05",
                null,
                "new-image.jpg"
        );
    }

    @Test
    void createAccommodation_success() {
        when(accommodationMapper.toEntity(oldAccommodationRequest, user)).thenReturn(existingAccommodation);
        when(accommodationRepository.save(existingAccommodation)).thenReturn(updatedAccommodation);
        when(accommodationMapper.toDetail(updatedAccommodation)).thenReturn(updatedAccommodationResponseDetail);
        doNothing().when(accommodationServiceHelper).validateAccommodationNameDoesNotExist(oldAccommodationRequest.name());

        AccommodationResponseDetail result = accommodationService.createAccommodation(oldAccommodationRequest, user);

        assertEquals(updatedAccommodationResponseDetail, result);
        verify(accommodationMapper).toEntity(oldAccommodationRequest, user);
        verify(accommodationRepository).save(existingAccommodation);
        verify(accommodationMapper).toDetail(updatedAccommodation);
        verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist(oldAccommodationRequest.name());
    }

    @Test
    void testUpdateAccommodation_whenNameChanged_callsValidateAndUpdatesFields() {
        Long id = existingAccommodation.getId();

        when(accommodationRepository.findById(id)).thenReturn(Optional.of(existingAccommodation));

        doNothing().when(accommodationServiceHelper).validateAccommodationNameDoesNotExist("New Name");
        doAnswer(invocation -> null).when(entityUtil).updateField(any(), any(), any());

        when(accommodationRepository.save(existingAccommodation)).thenReturn(updatedAccommodation);
        when(accommodationMapper.toDetail(updatedAccommodation)).thenReturn(updatedAccommodationResponseDetail);

        AccommodationResponseDetail result = accommodationService.updateAccommodation(id, newAccommodationRequest);

        verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist("New Name");
        verify(accommodationRepository).save(existingAccommodation);
        verify(accommodationMapper).toDetail(updatedAccommodation);
        assertEquals(result, updatedAccommodationResponseDetail);
    }
}
