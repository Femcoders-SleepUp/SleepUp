package com.SleepUp.SU.user.dto;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.dto.USER.UserRequest;
import com.SleepUp.SU.user.role.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapperDto {

    UserResponse fromEntity(User user);

    @Mapping(target = "roles", expression = "java(Set.of(com.SleepUp.SU.user.role.Role.USER))")
    User toEntity(UserRequest userRequest);

    User toEntityAdmin(UserRequest userRequest);

}
