package com.SleepUp.SU.user;

import com.SleepUp.SU.user.dto.USER.UserRequest;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserServiceHelper userServiceHelper;


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
            userSaved.setRoles(Set.of(Role.USER));

            when(userServiceHelper.getUserLogin("userTest")).thenReturn(Optional.of(userSaved));

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UserDetails userLogExpected = new org.springframework.security.core.userdetails.User(
                    "userTest",
                    "usertest@test.com",
                    true,
                    true,
                    true,
                    true,
                    authorities);

            UserDetails userLogResponse = userService.loadUserByUsername("userTest");

            assertEquals(userLogExpected, userLogResponse);

        }

        @Test
        void should_loginExistingUser_throw_exception() {

            when(userServiceHelper.getUserLogin("userTest"))
                    .thenThrow(new UsernameNotFoundException("userTest does not exist."));

            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("userTest"));
        }

    }
}
