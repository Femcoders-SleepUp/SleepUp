package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import com.SleepUp.SU.utils.EntityUtil;
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

        if(filter.fromDate() != null && filter.toDate() != null){
            EntityUtil.validateCheckInOutDates(filter.fromDate(), filter.toDate());
        }

        Specification<Accommodation> spec = accommodationSpecification.buildSpecification(filter);
        Page<Accommodation> accommodationPage = accommodationRepository.findAll(spec, pageable);

        return accommodationPage.map(accommodationMapper::toSummary);
    }
}
