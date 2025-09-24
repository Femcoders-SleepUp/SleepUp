package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.utils.EntityUtil;
import jakarta.transaction.Transactional;
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
    private final EntityUtil entityUtil;

    public List<AccommodationResponseSummary> getAllAccommodations(){
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return accommodations.stream().map(accommodationMapper::toSummary).toList();
    }

    public AccommodationResponseDetail getAccommodationById(Long id) {
        return accommodationMapper.toDetail(accommodationServiceHelper.getAccommodationEntityById(id));
    }

    @Transactional
    public AccommodationResponseDetail createAccommodation(AccommodationRequest accommodationRequest, User user){
        accommodationServiceHelper.validateAccommodationNameDoesNotExist(accommodationRequest.name());
        Accommodation accommodation = accommodationMapper.toEntity(accommodationRequest, user);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDetail(savedAccommodation);
    }

    public AccommodationResponseDetail updateAccommodation(Long id, AccommodationRequest accommodationRequest){
        Accommodation accommodation = accommodationServiceHelper.getAccommodationEntityById(id);
        if (!accommodation.getName().equals(accommodationRequest.name())){
            accommodationServiceHelper.validateAccommodationNameDoesNotExist(accommodationRequest.name());
        }
        entityUtil.updateField(accommodationRequest.name(), accommodation::getName, accommodation::setName);
        entityUtil.updateField(accommodationRequest.price(), accommodation::getPrice, accommodation::setPrice);
        entityUtil.updateField(accommodationRequest.guestNumber(), accommodation::getGuestNumber, accommodation::setGuestNumber);
        entityUtil.updateField(accommodationRequest.location(), accommodation::getLocation, accommodation::setLocation);
        entityUtil.updateField(accommodationRequest.description(), accommodation::getDescription, accommodation::setDescription);
        entityUtil.updateField(accommodationRequest.imageUrl(), accommodation::getImageUrl, accommodation::setImageUrl);
        entityUtil.updateField(accommodationRequest.checkInTime(), accommodation::getCheckInTime, accommodation::setCheckInTime);
        entityUtil.updateField(accommodationRequest.checkOutTime(), accommodation::getCheckOutTime, accommodation::setCheckOutTime);
        entityUtil.updateField(accommodationRequest.availableFrom(), accommodation::getAvailableFrom, accommodation::setAvailableFrom);
        entityUtil.updateField(accommodationRequest.availableTo(), accommodation::getAvailableTo, accommodation::setAvailableTo);

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDetail(savedAccommodation);
    }

    public void deleteAccommodation(Long id) {
        Accommodation accommodation = accommodationServiceHelper.getAccommodationEntityById(id);
        accommodationRepository.delete(accommodation);
    }
}