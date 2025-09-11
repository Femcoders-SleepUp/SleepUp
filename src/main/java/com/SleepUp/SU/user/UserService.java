package com.SleepUp.SU.user;

import com.SleepUp.SU.user.dto.USER.UserRequest;
import com.SleepUp.SU.user.dto.UserMapperDto;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.user.utils.UserSecurityUtils;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.utils.ApiMessageDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static sun.tools.jconsole.Messages.ERROR;

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

    public UserResponse registerUser(UserRequest request) {
        try {
            userServiceHelper.checkUsername(request.username());
            userServiceHelper.checkEmail(request.email());

            User user = UserMapperDto.INSTANCE.toEntity(request);
            user.setPassword(userServiceHelper.getEncodePassword(request.password()));
            user.setRoles(Set.of(Role.USER));

            User savedUser = userRepository.save(user);

            return UserMapperDto.INSTANCE.fromEntity(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Username or email already exists");
        }
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));
    }


    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapperDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser
                .map(userMapperDto::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    @Transactional
    public ResponseEntity<ApiMessageDto> userLogout(HttpServletRequest request, HttpServletResponse response){

    }
}

