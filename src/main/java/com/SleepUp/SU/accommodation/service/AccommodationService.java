package com.SleepUp.SU.accommodation.service;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.entity.User;

import java.util.List;

public interface AccommodationService {
    List<AccommodationResponseSummary> getAllAccommodations();

    AccommodationResponseDetail getAccommodationById(Long id);

    AccommodationResponseDetail createAccommodation(AccommodationRequest accommodationRequest, User user);

    AccommodationResponseDetail updateAccommodation(Long id, AccommodationRequest accommodationRequest);

    void deleteAccommodation(Long id);
}