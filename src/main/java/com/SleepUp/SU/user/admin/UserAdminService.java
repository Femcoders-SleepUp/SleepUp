package com.SleepUp.SU.user.admin;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityUtil mapperUtil;
    private final UserServiceHelper userServiceHelper;
    private final PasswordEncoder passwordEncoder;


    public List<UserResponse> getAllUsers() {
        return mapperUtil.mapEntitiesToDTOs(userRepository.findAll(), userMapper::toResponse);
    }

    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(userServiceHelper.findById(userId));
    }

    public UserResponse createUser(UserRequestAdmin userRequestAdmin) {
        if (userRepository.findByUsername(userRequestAdmin.username()).isPresent()) {
            throw new RuntimeException("Username already exists " + userRequestAdmin.username());
        }

        String encodedPassword = passwordEncoder.encode(userRequestAdmin.password());
        User user = userMapper.toEntityAdmin(userRequestAdmin, encodedPassword);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public UserResponse updateUser(Long userId, UserRequestAdmin userRequestAdmin) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        existingUser.setUsername(userRequestAdmin.username());
        existingUser.setName(userRequestAdmin.name());
        existingUser.setEmail(userRequestAdmin.email());

        if (userRequestAdmin.password() != null && !userRequestAdmin.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequestAdmin.password()));
        }

        existingUser.setRole(userRequestAdmin.role());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponse(updatedUser);
    }

    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User with id" + userId + "does not exist.");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userServiceHelper.findByUsername(username);
        return new CustomUserDetails(user);
    }
}
