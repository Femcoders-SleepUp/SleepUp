package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;

public interface UserUserService {
    UserResponse getLoggedUser(Long id);

    UserResponse updateLoggedUser(UserRequest userRequest, Long id);

    void deleteMyUser(Long id);
}
