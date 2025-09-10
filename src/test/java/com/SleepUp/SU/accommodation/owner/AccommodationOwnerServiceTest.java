package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.utils.EntityMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccommodationOwnerServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private EntityMapperUtil mapperUtil;

    @Mock
    private AccommodationMapper accommodationMapper;

    @InjectMocks
    private AccommodationOwnerService accommodationOwnerService;

    private Long userId;
    private User user;
    private List<Accommodation> accommodations;
    private List<AccommodationResponseSummary> expectedSummaries;

    private Accommodation accommodation1;
    private Accommodation accommodation2;
    private AccommodationResponseSummary summary1;
    private AccommodationResponseSummary summary2;

    @BeforeEach
    void setUp() {

        userId = 1L;

        user = User.builder()
                .id(userId)
                .build();

        accommodation1 = Accommodation.builder()
                .id(101L)
                .name("Sea View Apartment")
                .price(150.0)
                .guestNumber(4)
                .location("Beach")
                .description("A lovely sea view apartment")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.of(2025, 6, 1))
                .availableTo(LocalDate.of(2025, 12, 31))
                .managedBy(user)
                .build();

        accommodation2 = Accommodation.builder()
                .id(102L)
                .name("Mountain Cabin")
                .price(200.0)
                .guestNumber(6)
                .location("Mountains")
                .description("Cozy cabin in the mountains")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(10, 0))
                .availableFrom(LocalDate.of(2025, 7, 1))
                .availableTo(LocalDate.of(2025, 11, 30))
                .managedBy(user)
                .build();

        accommodations = Arrays.asList(accommodation1, accommodation2);

        summary1 = new AccommodationResponseSummary(
                "Sea View Apartment",
                150.0,
                4,
                "Beach",
                "image1.jpg"
        );

        summary2 = new AccommodationResponseSummary(
                "Mountain Cabin",
                200.0,
                6,
                "Mountains",
                "image2.jpg"
        );

        expectedSummaries = Arrays.asList(summary1, summary2);
    }

    @Nested
    class getAllAccommodationsByOwnerId {

        @SuppressWarnings("unchecked")
        @Test
        void getAllAccommodationsByOwnerId_shouldReturnAccommodationsSummariesList() {
            when(accommodationRepository.findByManagedBy_Id(userId)).thenReturn(accommodations);
            when(mapperUtil.mapEntitiesToDTOs(eq(accommodations), (Function<Accommodation, AccommodationResponseSummary>) any(Function.class))).thenReturn(expectedSummaries);

            List<AccommodationResponseSummary> actualSummaries = accommodationOwnerService.getAllAccommodationsByOwnerId(userId);

            assertEquals(expectedSummaries, actualSummaries);

            verify(accommodationRepository).findByManagedBy_Id(userId);
            verify(mapperUtil).mapEntitiesToDTOs(eq(accommodations), (Function<Accommodation, AccommodationResponseSummary>) any(Function.class));
            verifyNoMoreInteractions(accommodationRepository, mapperUtil, accommodationMapper);
        }

        @SuppressWarnings("unchecked")
        @Test
        void getAllAccommodationsByOwnerId_shouldReturnEmptyList() {
            when(accommodationRepository.findByManagedBy_Id(userId)).thenReturn(List.of());
            when(mapperUtil.mapEntitiesToDTOs(eq(List.of()), (Function<Accommodation, AccommodationResponseSummary>) any(Function.class))).thenReturn(List.of());

            List<AccommodationResponseSummary> summaries = accommodationOwnerService.getAllAccommodationsByOwnerId(userId);

            assertTrue(summaries.isEmpty());

            verify(accommodationRepository).findByManagedBy_Id(userId);
            verify(mapperUtil).mapEntitiesToDTOs(eq(List.of()), (Function<Accommodation, AccommodationResponseSummary>) any(Function.class));
            verifyNoMoreInteractions(accommodationRepository, mapperUtil, accommodationMapper);
        }
    }
}
