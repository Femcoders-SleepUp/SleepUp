package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final AccommodationServiceHelper accommodationServiceHelper;

    public List<AccommodationResponseSummary> getAllAccommodations(){
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return accommodations.stream().map(accommodationMapper::toSummary).toList();
    }
    
    public Optional<AccommodationResponseDetail> getAccommodationById(Long id) {
        return accommodationRepository.findById(id).map(accommodationMapper :: toDetail);
    }

    public AccommodationResponseDetail createAccommodation(AccommodationRequest accommodationRequest, User user){
        accommodationServiceHelper.validateAccommodationNameDoesNotExist(accommodationRequest.name());
        Accommodation accommodation = accommodationMapper.toEntity(accommodationRequest, user);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDetail(savedAccommodation);
    }
}