package com.SleepUp.SU.user;

import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.EntityMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityMapperUtil mapperUtil;
    private final UserServiceHelper userServiceHelper;


    public List<UserResponse> getAllUsers() {
        return mapperUtil.mapEntitiesToDTOs(userRepository.findAll(), userMapper::toResponse);
    }

    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(userServiceHelper.findById(userId));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userServiceHelper.findByUsername(username);
        return new CustomUserDetails(user);
    }
}

