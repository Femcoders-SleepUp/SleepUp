package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserMapperDtoImpl;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {

    @InjectMocks
    private UserAdminService userService;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapperDtoImpl userMapper;


    @Nested
    class LoadUserTest {

        @Test
        void should_loadExistingUser_fromRequest() {
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
        void should_loadExistingUser_throw_exception() {

            when(userServiceHelper.findByUsername("userTest"))
                    .thenThrow(new UsernameNotFoundException("userTest does not exist."));

            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("userTest"));
        }

    }

    @Nested
    class GetAllUsersTest {
        @Test
        void should_returnAllUsers() {
            User user1 = new User();
            user1.setId(1L);
            user1.setUsername("userOne");
            user1.setName("User One");
            user1.setEmail("user1@test.com");

            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("userTwo");
            user2.setName("User Two");
            user2.setEmail("user2@test.com");

            when(userRepository.findAll()).thenReturn(List.of(user1, user2));

            when(userMapper.fromEntity(user1)).thenReturn(new UserResponse(user1.getId(), user1.getUsername(), user1.getName(), user1.getEmail(), Role.USER
            ));

            when(userMapper.fromEntity(user2)).thenReturn(new UserResponse(user2.getId(), user2.getUsername(), user2.getName(), user2.getEmail(), Role.USER
            ));

            List<UserResponse> result = userService.getAllUsers();

            assertEquals(2, result.size());
            assertEquals("userOne",result.get(0).username());
            assertEquals("userTwo", result.get(1).username());

            verify(userRepository, times(1)).findAll();

        }
    }

    @Nested
    class GetUserByIdTest {

        @Test
        void should_returnUserById_whenExists() {

            User user = new User();
            user.setId(1L);
            user.setUsername("userOne");
            user.setName("User One");
            user.setEmail("user1@test.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.fromEntity(user)).thenReturn(new UserResponse(user.getId(), user.getUsername(), user.getName(), user.getEmail(), Role.USER
            ));

          UserResponse result = userService.getUserById(1L);

            assertEquals("userOne", result.username());
            assertEquals("user1@test.com", result.email());

            verify(userRepository, times(1)).findById(1L);

        }

        @Test
        void should_trowException_whenUserNotFound() {

            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> userService.getUserById(99L));

            verify(userRepository, times(1)).findById(99L);
        }
    }
}
