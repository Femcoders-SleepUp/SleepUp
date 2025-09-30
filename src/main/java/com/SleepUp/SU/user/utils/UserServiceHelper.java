package com.SleepUp.SU.user.utils;

import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.exceptions.UserEmailAlreadyExistsException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByIdException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByUsernameException;
import com.SleepUp.SU.utils.exceptions.UserUsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceHelper {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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


    @Transactional
    public User createUser(UserRequest request, Role role){
        validateUserDoesNotExist(request.username(), request.email());
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, encodedPassword, role);
        return userRepository.save(user);
    }

    @Transactional
    public void updateUserData(UserRequest request, User user) {
        User isExistingUsername = userRepository.findByUsername(request.username())
                .orElse(user);
        User isExistingEmail = userRepository.findByEmail(request.email())
                .orElse(user);

        if (isExistingUsername.getId() != user.getId() || isExistingEmail.getId() != user.getId()){
            throw new RuntimeException("This username or email already exist");
        }


        String username = request.username() != null && !request.username().isEmpty()
                ? request.username() :
                user.getUsername();

        String name = request.name() != null && !request.name().isEmpty()
                ? request.name() :
                user.getName();

        String email = request.email() != null && !request.email().isEmpty()
                ? request.email() :
                user.getEmail();

        String password = request.password() != null && !request.password().isEmpty()
                ? this.getEncodePassword(request.password()) :
                user.getPassword();

        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
    }

    @Transactional
    public void updateUserDataAdmin(UserRequestAdmin userRequestAdmin, User user){
        UserRequest userData = new UserRequest(
                userRequestAdmin.username(),
                userRequestAdmin.name(),
                userRequestAdmin.email(),
                userRequestAdmin.password()
        );

        updateUserData(userData, user);

        Role role = userRequestAdmin.role() != null && !userRequestAdmin.role().getRoleName().isEmpty()
                ? userRequestAdmin.role() :
                user.getRole();

        user.setRole(role);
    }


}
