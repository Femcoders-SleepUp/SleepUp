package com.SleepUp.SU.accommodation.filter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
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

        FilterAccommodationDTO filter = new FilterAccommodationDTO(
                "Hotel", "Nice place", 50.0, 200.0, 2, "New York", LocalDate.now(), LocalDate.now().plusDays(5));

        Pageable pageable = PageRequest.of(0, 10);

        Specification<Accommodation> spec = mock(Specification.class);
        when(accommodationSpecification.buildSpecification(filter)).thenReturn(spec);

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setName("Hotel ABC");

        Page<Accommodation> accommodationPage = new PageImpl<>(List.of(accommodation), pageable, 1);
        when(accommodationRepository.findAll(spec, pageable)).thenReturn(accommodationPage);

        AccommodationResponseSummary dto = new AccommodationResponseSummary( "Hotel ABC", 10.0, 1, "null", "image.url");

        when(accommodationMapper.toSummary(accommodation)).thenReturn(dto);

        Page<AccommodationResponseSummary> result = accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);

        verify(accommodationSpecification).buildSpecification(filter);
        verify(accommodationRepository).findAll(spec, pageable);
        verify(accommodationMapper).toSummary(accommodation);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Hotel ABC", result.getContent().get(0).name());
    }
}
