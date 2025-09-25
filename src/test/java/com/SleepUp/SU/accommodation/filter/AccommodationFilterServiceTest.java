package com.SleepUp.SU.accommodation.filter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import com.SleepUp.SU.exceptions.InvalidDateRangeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AccommodationFilterServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationSpecification accommodationSpecification;

    @Mock
    private AccommodationMapper accommodationMapper;

    @InjectMocks
    private AccommodationFilterService accommodationFilterService;

    @Test
    void testGetAllFilteredAccommodationsWithPagination() {
        FilterAccommodationDTO filter = new FilterAccommodationDTO("Hotel", "Nice place", 50.0, 200.0, 2, "New York", LocalDate.now(), LocalDate.now().plusDays(5));
        Pageable pageable = PageRequest.of(0, 10);
        AccommodationResponseSummary expectedDto = new AccommodationResponseSummary( 1L, "Hotel ABC", 10.0, 1, true, "Park Av", "image.url");
        Accommodation accommodation = Accommodation.builder()
                .id(1L)
                .name("Hotel ABC")
                .price(10.0)
                .guestNumber(1)
                .location("Park Av")
                .imageUrl("image.jpg")
                .build();
        Specification<Accommodation> spec = mock(Specification.class);
        Page<Accommodation> accommodationPage = new PageImpl<>(List.of(accommodation), pageable, 1);

        when(accommodationSpecification.buildSpecification(filter)).thenReturn(spec);
        when(accommodationRepository.findAll(spec, pageable)).thenReturn(accommodationPage);
        when(accommodationMapper.toSummary(accommodation)).thenReturn(expectedDto);

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(spec, pageable);
        verify(accommodationMapper).toSummary(accommodation);

        assertEquals(new PageImpl<>(List.of(expectedDto), pageable, 1), result);
        assertEquals(expectedDto, result.getContent().getFirst());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllFilteredAccommodationsWithPagination_checkOutBeforeCheckIn() {
        FilterAccommodationDTO filter = new FilterAccommodationDTO("Hotel", "Nice place", 50.0, 200.0, 2, "New York", LocalDate.now().plusDays(10), LocalDate.now().plusDays(5));
        Pageable pageable = PageRequest.of(0, 10);

        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable)
        );

        assertEquals("Check-in date must be before check-out date", exception.getMessage());

    }

    @Test
    void testGetAllFilteredAccommodationsWithPagination_checkInBeforeToday() {
        FilterAccommodationDTO filter = new FilterAccommodationDTO("Hotel", "Nice place", 50.0, 200.0, 2, "New York", LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));
        Pageable pageable = PageRequest.of(0, 10);

        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable)
        );

        assertEquals("Check-in date cannot be in the past", exception.getMessage());

    }
}
