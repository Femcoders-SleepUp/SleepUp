package com.SleepUp.SU.user.utils;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.utils.exceptions.UserEmailAlreadyExistsException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByIdException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByUsernameException;
import com.SleepUp.SU.utils.exceptions.UserUsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceHelper {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundByIdException(id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundByUsernameException(username));
    }

    public void validateUserDoesNotExist(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new UserUsernameAlreadyExistsException(username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserEmailAlreadyExistsException(email);
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

    public void updateUserDataAdmin(UserRequestAdmin request, User user) {
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
