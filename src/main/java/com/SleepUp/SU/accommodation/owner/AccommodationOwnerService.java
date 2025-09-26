package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import java.util.List;

public interface AccommodationOwnerService {
    List<AccommodationResponseSummary> getAllAccommodationsByOwnerId(Long userId);
}
