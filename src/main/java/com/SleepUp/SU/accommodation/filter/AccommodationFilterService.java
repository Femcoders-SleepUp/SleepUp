package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationFilterService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final AccommodationSpecification accommodationSpecification;

    public Page<AccommodationResponseSummary> getAllFilteredAccommodationsWithPagination(
            FilterAccommodationDTO filter,
            Pageable pageable) {

        Specification<Accommodation> spec = accommodationSpecification.buildSpecification(filter);
        Page<Accommodation> accommodationPage = accommodationRepository.findAll(spec, pageable);

        return accommodationPage.map(accommodationMapper::toSummary);
    }
}
