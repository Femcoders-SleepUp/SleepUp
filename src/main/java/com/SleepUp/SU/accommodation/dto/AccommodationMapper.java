package com.SleepUp.SU.accommodation.dto;

import com.SleepUp.SU.accommodation.Accommodation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    AccommodationResponseSummary toSummary(Accommodation accommodation);
}
