package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserAdminService extends UserDetailsService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse createUser(UserRequestAdmin userRequestAdmin);

    UserResponse updateUser(Long userId, UserRequestAdmin userRequestAdmin);

    void deleteUserById(Long id);
}
