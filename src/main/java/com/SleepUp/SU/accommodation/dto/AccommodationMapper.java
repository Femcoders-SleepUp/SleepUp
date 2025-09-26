package com.SleepUp.SU.accommodation.dto;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    AccommodationResponseSummary toSummary(Accommodation accommodation);

    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "managedBy", source = "user")
    @Mapping(target = "name", source = "accommodationRequest.name")
    @Mapping(target = "guestNumber", source = "accommodationRequest.guestNumber")
    Accommodation toEntity(AccommodationRequest accommodationRequest, User user);

    @Mapping(target = "managedByUsername", source = "accommodation.managedBy.name")
    AccommodationResponseDetail toDetail(Accommodation accommodation);
}