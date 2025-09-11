package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserService;
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
    private final UserService USER_SERVICE;


    public List<AccommodationResponseSummary> getAllAccommodationsByOwnerId(){
        User user = USER_SERVICE.getAuthenticatedUser();
        List<Accommodation> accommodations = accommodationRepository.findByManagedBy_Id(user.getId());
        return mapperUtil.mapEntitiesToDTOs(accommodations, accommodationMapper::toSummary);
    }
}
