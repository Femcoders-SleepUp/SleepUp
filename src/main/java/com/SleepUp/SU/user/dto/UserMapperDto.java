package com.SleepUp.SU.user.dto;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.dto.USER.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapperDto {

    UserMapperDto INSTANCE = Mappers.getMapper(UserMapperDto.class);

    UserResponse fromEntity(User user);

    User toEntity(UserRequest userRequest);

    User toEntityAdmin(UserRequest userRequest);
}
