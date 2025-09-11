package com.SleepUp.SU.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceHelper {
    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Username by id does not exist"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Username by username does not exist"));
    }

    public void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("already exists username");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("already exists email");
        }
    }
}
