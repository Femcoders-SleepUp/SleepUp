package com.SleepUp.SU.user.utils;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceHelper {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


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

    public String getEncodePassword(String password) {
        return passwordEncoder.encode(password);
    }


    public void updateUserData(UserRequest request, User user) {
        validateUserDoesNotExist(user.getUsername(), user.getEmail());

        String username = request.username() != null && !request.username().isEmpty()
                ? request.username() :
                user.getUsername();

        String email = request.email() != null && !request.email().isEmpty()
                ? request.email() :
                user.getEmail();

        String password = request.password() != null && !request.password().isEmpty()
                ? this.getEncodePassword(request.password()) :
                user.getPassword();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

    }
}
