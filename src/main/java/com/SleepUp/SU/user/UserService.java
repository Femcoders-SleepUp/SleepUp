package com.SleepUp.SU.user;

import com.SleepUp.SU.user.dto.UserMapperDto;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.utils.UserSecurityUtils;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserServiceHelper userServiceHelper;
    private final UserMapperDto userMapperDto;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userServiceHelper.getUserLogin(username);
        User user = optionalUser.orElseThrow();
        List<GrantedAuthority> authorities = UserSecurityUtils.getAuthoritiesRole(user);

        return UserSecurityUtils.createUserByUserDetails(user, authorities);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapperDto::fromEntity)
                .toList();
    }
}

