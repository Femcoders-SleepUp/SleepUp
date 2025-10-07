package com.SleepUp.SU.user.dto;

import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.role.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "password", source = "encodedPassword")
    User toEntity(UserRequest userRequest,  String encodedPassword, Role role);
}