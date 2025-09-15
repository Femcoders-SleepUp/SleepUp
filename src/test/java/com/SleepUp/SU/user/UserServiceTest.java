package com.SleepUp.SU.user;

import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;


    @Nested
    class LoginUserTest {

        @Test
        void should_loginExistingUser_fromRequest() {
            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");
            User userSaved = new User();
            userSaved.setId(1L);
            userSaved.setUsername("userTest");
            userSaved.setName("nameTest");
            userSaved.setEmail("usertest@test.com");
            userSaved.setPassword("password123");

            when(userServiceHelper.findByUsername("userTest")).thenReturn(userSaved);

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UserDetails userLogExpected = new CustomUserDetails(userSaved);


            UserDetails userLogResponse = userService.loadUserByUsername("userTest");

            assertEquals(userLogExpected.getUsername(), userLogResponse.getUsername());
            assertEquals(userLogExpected.getAuthorities(), userLogResponse.getAuthorities());
            assertEquals(userLogExpected.getPassword(), userLogResponse.getPassword());


        }

        @Test
        void should_loginExistingUser_throw_exception() {

            when(userServiceHelper.findByUsername("userTest"))
                    .thenThrow(new UsernameNotFoundException("userTest does not exist."));

            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("userTest"));
        }

    }
}