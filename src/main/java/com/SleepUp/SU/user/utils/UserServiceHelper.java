package com.SleepUp.SU.user.utils;

import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.EntityUtil;
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
    private final EntityUtil entityUtil;

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundByIdException(id));
    }

    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundByUsernameException(username));
    }

    public void validateUserDoesNotExist(String username, String email) {
        validateUsernameDoesNotExist(username);
        validateEmailDoesNotExist(email);
    }

    public void validateUsernameDoesNotExist(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserUsernameAlreadyExistsException(username);
        }
    }

    public void validateEmailDoesNotExist(String email) {
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

    public User updateUser(UserRequest request, User existingUser) {

        if(!request.username().equals(existingUser.getUsername())){
            validateUsernameDoesNotExist(request.username());
        }

        if(!request.email().equals(existingUser.getEmail())){
            validateUsernameDoesNotExist(request.username());
        }

        entityUtil.updateField(request.username(), existingUser::getUsername, existingUser::setUsername);
        entityUtil.updateField(request.email(), existingUser::getEmail, existingUser::setEmail);
        entityUtil.updateField(request.name(), existingUser::getName, existingUser::setName);

        String password = request.password() != null && !request.password().isEmpty()
                ? this.getEncodePassword(request.password()) :
                existingUser.getPassword();

        existingUser.setPassword(password);

        return existingUser;

    }

    @Transactional
    public User updateUserData(UserRequest request, User user) {
        return updateUser(request, user);
    }

    @Transactional
    public User updateUserDataAdmin(UserRequestAdmin userRequestAdmin, User user){
        UserRequest userData = new UserRequest(
                userRequestAdmin.username(),
                userRequestAdmin.name(),
                userRequestAdmin.email(),
                userRequestAdmin.password()
        );

        User updatedUser = updateUser(userData, user);

        Role role = userRequestAdmin.role() != null
                ? userRequestAdmin.role() :
                updatedUser.getRole();

        updatedUser.setRole(role);
        return updatedUser;
    }

}
