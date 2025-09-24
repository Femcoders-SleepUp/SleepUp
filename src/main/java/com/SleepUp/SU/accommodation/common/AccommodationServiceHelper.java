package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccommodationServiceHelper {
    private final AccommodationRepository accommodationRepository;

    public void validateAccommodationNameDoesNotExist(String name) {
        if (accommodationRepository.existsByName(name)) {
            throw new AccommodationAlreadyExistsByNameException(name);
        }
    }

    public Accommodation getAccommodationEntityById(Long id) {
        return accommodationRepository.findById(id).orElseThrow(() -> new AccommodationNotFoundByIdException(id));
    }
}