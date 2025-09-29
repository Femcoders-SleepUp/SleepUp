package com.SleepUp.SU.accommodation.filter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import com.SleepUp.SU.exceptions.InvalidDateRangeException;
import org.junit.jupiter.api.BeforeEach;
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

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    private FilterAccommodationDTO createFilter(LocalDate fromDate, LocalDate toDate) {
        return FilterAccommodationDTO.builder()
                .name("Hotel")
                .description("Nice place")
                .minPrice(50.0)
                .maxPrice(200.0)
                .guestNumber(2)
                .location("New York")
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }

    private void mockSpecificationAndRepository(FilterAccommodationDTO filter,
                                                List<Accommodation> accommodations) {
        Specification<Accommodation> spec = mock(Specification.class);
        Page<Accommodation> accommodationPage = new PageImpl<>(accommodations, pageable, accommodations.size());
        when(accommodationSpecification.buildSpecification(filter)).thenReturn(spec);
        when(accommodationRepository.findAll(spec, pageable)).thenReturn(accommodationPage);
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_validFilter_shouldReturnPageOfAccommodationSummaries() {
        FilterAccommodationDTO filter = createFilter(LocalDate.now(), LocalDate.now().plusDays(5));

        AccommodationResponseSummary expectedDto = AccommodationResponseSummary.builder()
                .id(1L)
                .name("Hotel ABC")
                .price(10.0)
                .guestNumber(1)
                .petFriendly(true)
                .location("Park Av")
                .imageUrl("image.url")
                .build();

        Accommodation accommodation = Accommodation.builder()
                .id(1L)
                .name("Hotel ABC")
                .price(10.0)
                .guestNumber(1)
                .location("Park Av")
                .imageUrl("image.jpg")
                .build();

        mockSpecificationAndRepository(filter, List.of(accommodation));
        when(accommodationMapper.toSummary(accommodation)).thenReturn(expectedDto);

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(
                (Specification<Accommodation>) any(), eq(pageable));
        verify(accommodationMapper).toSummary(accommodation);

        assertEquals(new PageImpl<>(List.of(expectedDto), pageable, 1), result);
        assertEquals(expectedDto, result.getContent().get(0));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_nullDates_shouldSkipDateValidationAndReturnEmptyPage() {
        FilterAccommodationDTO filter = createFilter(null, null);

        mockSpecificationAndRepository(filter, List.of());

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(
                (Specification<Accommodation>) any(), eq(pageable));

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_nullFromDate_shouldSkipDateValidationAndReturnEmptyPage() {
        FilterAccommodationDTO filter = createFilter(null, LocalDate.now().plusDays(5));

        mockSpecificationAndRepository(filter, List.of());

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(
                (Specification<Accommodation>) any(), eq(pageable));

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_nullToDate_shouldSkipDateValidationAndReturnEmptyPage() {
        FilterAccommodationDTO filter = createFilter(LocalDate.now(), null);

        mockSpecificationAndRepository(filter, List.of());

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(
                (Specification<Accommodation>) any(), eq(pageable));

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_nonNullDates_shouldInvokeDateValidationAndReturnEmptyPage() {
        FilterAccommodationDTO filter = createFilter(LocalDate.now(), LocalDate.now().plusDays(5));

        mockSpecificationAndRepository(filter, List.of());

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(
                (Specification<Accommodation>) any(), eq(pageable));

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_checkOutBeforeCheckIn_shouldThrowInvalidDateRangeException() {
        FilterAccommodationDTO filter = createFilter(LocalDate.now().plusDays(10), LocalDate.now().plusDays(5));

        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable)
        );

        assertEquals("Check-in date must be before check-out date", exception.getMessage());
    }

    @Test
    void getAllFilteredAccommodationsWithPagination_checkInBeforeToday_shouldThrowInvalidDateRangeException() {
        FilterAccommodationDTO filter = createFilter(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5));

        InvalidDateRangeException exception = assertThrows(
                InvalidDateRangeException.class,
                () -> accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable)
        );

        assertEquals("Check-in date cannot be in the past", exception.getMessage());
    }
}
