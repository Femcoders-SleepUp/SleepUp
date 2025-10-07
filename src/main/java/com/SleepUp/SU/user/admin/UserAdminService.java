package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;

import java.util.List;

public interface UserAdminService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse createUser(UserRequest userRequest, Role role);

    UserResponse updateUser(Long userId, UserRequestAdmin userRequestAdmin);

    void deleteUserById(Long id);

}