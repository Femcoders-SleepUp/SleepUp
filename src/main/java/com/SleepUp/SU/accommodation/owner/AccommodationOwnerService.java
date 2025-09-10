package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.utils.EntityMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccommodationOwnerService {
    private final AccommodationRepository accommodationRepository;
    private final EntityMapperUtil mapperUtil;
    private final AccommodationMapper accommodationMapper;

    public List<AccommodationResponseSummary> getAllAccommodationsByOwnerId(Long userId){
        List<Accommodation> accommodations = accommodationRepository.findByManagedBy_Id(userId);
        return mapperUtil.mapEntitiesToDTOs(accommodations, accommodationMapper::toSummary);
    }
}
