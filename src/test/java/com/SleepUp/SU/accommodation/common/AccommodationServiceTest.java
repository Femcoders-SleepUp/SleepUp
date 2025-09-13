package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @Mock
    private AccommodationMapper accommodationMapper;

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private AccommodationService accommodationService;

    @Mock
    private AccommodationServiceHelper accommodationServiceHelper;

    private AccommodationRequest accommodationRequest;
    private User user;
    private Accommodation accommodation;
    private Accommodation savedAccommodation;
    private AccommodationResponseDetail accommodationResponseDetail;

    @BeforeEach
    void setUp() {
        accommodationRequest = new AccommodationRequest(
                "Name", 100.0, 2, "Location", "Description",
                LocalTime.of(14, 0), LocalTime.of(12, 0),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "image.jpg"
        );

        user = new User();

        accommodation = new Accommodation();

        savedAccommodation = new Accommodation();

        accommodationResponseDetail = new AccommodationResponseDetail(
                "Name", 100.0, 2, "Location", "Description",
                "14:00", "12:00", "2025-01-01", "2025-12-31", "username", "imageUrl"
        );
    }


    @Test
    void createAccommodation_success() {
        when(accommodationMapper.toEntity(accommodationRequest, user)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(savedAccommodation);
        when(accommodationMapper.toDetail(savedAccommodation)).thenReturn(accommodationResponseDetail);
        doNothing().when(accommodationServiceHelper).validateAccommodationNameDoesNotExist(accommodationRequest.name());

        AccommodationResponseDetail result = accommodationService.createAccommodation(accommodationRequest, user);

        assertEquals(accommodationResponseDetail, result);
        verify(accommodationMapper).toEntity(accommodationRequest, user);
        verify(accommodationRepository).save(accommodation);
        verify(accommodationMapper).toDetail(savedAccommodation);
        verify(accommodationServiceHelper).validateAccommodationNameDoesNotExist(accommodationRequest.name());
    }
}
