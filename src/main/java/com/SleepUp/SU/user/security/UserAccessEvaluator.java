package com.SleepUp.SU.user.security;

import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccessEvaluator {
    private final UserRepository userRepository;

    public boolean isAdmin(Long userId) {
        return userRepository.existsByIdAndRole(userId, Role.ADMIN);
    }

}
