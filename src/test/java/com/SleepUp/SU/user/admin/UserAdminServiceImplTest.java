package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByIdException;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserAdminServiceImplTest {

    @InjectMocks
    private UserAdminServiceImpl userAdminServiceImpl;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Nested
    class LoadUserTest {

        @Test
        void should_loadExistingUser_fromRequest() {
            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");
            User userSaved = new User();
            userSaved.setId(1L);
            userSaved.setUsername("userTest");
            userSaved.setName("nameTest");
            userSaved.setEmail("usertest@email.com");
            userSaved.setPassword("password123");

            when(userServiceHelper.getUserEntityByUsername("userTest")).thenReturn(userSaved);

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UserDetails userLogExpected = new CustomUserDetails(userSaved);


            UserDetails userLogResponse = userAdminServiceImpl.loadUserByUsername("userTest");

            assertEquals(userLogExpected.getUsername(), userLogResponse.getUsername());
            assertEquals(userLogExpected.getAuthorities(), userLogResponse.getAuthorities());
            assertEquals(userLogExpected.getPassword(), userLogResponse.getPassword());


        }

        @Test
        void should_loadExistingUser_throw_exception() {

            when(userServiceHelper.getUserEntityByUsername("userTest"))
                    .thenThrow(new UsernameNotFoundException("userTest does not exist."));

            assertThrows(UsernameNotFoundException.class, () -> userAdminServiceImpl.loadUserByUsername("userTest"));
        }

    }

    @Nested
    class UpdateUserTest {

        @Test
        void updateUser_whenUserExists_shouldReturnUpdatedUserResponse() {

            UserRequestAdmin request = UserRequestAdmin.builder()
                    .username("updatedUser")
                    .name("Updated Name")
                    .email("updated@test.com")
                    .password("newPassword")
                    .role(Role.ADMIN)
                    .build();

            User existingUser = User.builder()
                    .id(1L)
                    .username("oldUser")
                    .name("Old Name")
                    .email("old@test.com")
                    .password("oldPassword")
                    .role(Role.USER)
                    .build();

            User updatedUser = User.builder()
                    .id(1L)
                    .username("updatedUser")
                    .name("Updated Name")
                    .email("updated@test.com")
                    .password("encodedPass")
                    .role(Role.ADMIN)
                    .build();

            UserResponse userResponse = new UserResponse(1L, "updatedUser", "Updated Name", "updated@test.com", Role.ADMIN);

            when(userServiceHelper.getUserEntityById(1L)).thenReturn(existingUser);
            when(userServiceHelper.updateUserDataAdmin(eq(request), any(User.class))).thenReturn(updatedUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            UserResponse response = userAdminServiceImpl.updateUser(1L, request);

            assertEquals("updatedUser", response.username());
            assertEquals("Updated Name", response.name());
            assertEquals("updated@test.com", response.email());
            assertEquals(Role.ADMIN, response.role());

        }

    }
}
