package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.utils.EntityMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityMapperUtil mapperUtil;
    private final UserServiceHelper userServiceHelper;

    public UserResponse getLoggedUser(Long id){
        return userMapper.toResponse(userServiceHelper.findById(id));
    }
}
