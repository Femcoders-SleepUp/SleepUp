package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {

    @InjectMocks
    private UserAdminService userAdminService;

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

            when(userServiceHelper.findByUsername("userTest")).thenReturn(userSaved);

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UserDetails userLogExpected = new CustomUserDetails(userSaved);


            UserDetails userLogResponse = userAdminService.loadUserByUsername("userTest");

            assertEquals(userLogExpected.getUsername(), userLogResponse.getUsername());
            assertEquals(userLogExpected.getAuthorities(), userLogResponse.getAuthorities());
            assertEquals(userLogExpected.getPassword(), userLogResponse.getPassword());


        }

        @Test
        void should_loadExistingUser_throw_exception() {

            when(userServiceHelper.findByUsername("userTest"))
                    .thenThrow(new UsernameNotFoundException("userTest does not exist."));

            assertThrows(UsernameNotFoundException.class, () -> userAdminService.loadUserByUsername("userTest"));
        }

    }

    @Nested
    class UpdateUserTest {
        @Test
        void should_updateExistingUser() {
            UserRequestAdmin request = new UserRequestAdmin(
                    "updatedUser",
                    "Updated Name",
                    "updated@test.com",
                    "newPassword",
                    Role.ADMIN
            );

            User existingUser = new User();
            existingUser.setId(1L);
            existingUser.setUsername("oldUser");
            existingUser.setName("Old Name");
            existingUser.setEmail("old@test.com");
            existingUser.setPassword("oldPassword");
            existingUser.setRole(Role.USER);

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            User savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername("updateUser");
            savedUser.setName("update Name");
            savedUser.setEmail("updated@test.com");
            savedUser.setPassword("encodedPass");
            savedUser.setRole(Role.ADMIN);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponse(any(User.class)))
                    .thenReturn(new UserResponse(1L, "updatedUser", "Updated Name", "updated@test.com", Role.ADMIN));

            UserResponse response = userAdminService.updateUser(1L, request);

            assertEquals("updatedUser", response.username());
            assertEquals("Updated Name", response.name());
            assertEquals("updated@test.com", response.email());
            assertEquals(Role.ADMIN, response.role());

            verify(userRepository, times(1)).save(any(User.class));

        }

        @Test
        void  should_throwException_when_userNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UserRequestAdmin request = new UserRequestAdmin(
                    "updatedUser",
                    "Updated Name",
                    "updated@test.com",
                    "password",
                    Role.ADMIN
            );

            assertThrows(RuntimeException.class, () -> userAdminService.updateUser(1L, request));


//            existingUser.setPassword("oldPass");
 //           existingUser.setRole(Role.USER);
//
 //           UserRequestAdmin updateRequest = new UserRequestAdmin(
//                    "newUsername",
//                    "New Name",
//                    "new@test.com",
//                    "newPass123",
//                    Role.ADMIN
//            );
//
//            User updatedUser = new User();
//            updatedUser.setId(1L);
//            updatedUser.setUsername("newUsername");
//            updatedUser.setName("Old Name");
//            updatedUser.setEmail("new@email.com");
//            updatedUser.setPassword("newPass123");
//            updatedUser.setRole(Role.USER);
//
//            UserResponse updatedUserResponse = new UserResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getName(), updatedUser.getEmail(), updatedUser.getRole());
//
//
//            when(userServiceHelper.findById(1L)).thenReturn(existingUser);
//            doAnswer(invocation -> {
//                existingUser.setUsername("newUsername");
//                existingUser.setEmail("new@email.com");
//                existingUser.setPassword("encodedPassword");
//                return null;
//            }).when(userServiceHelper).updateUserDataAdmin(updateRequest, existingUser);
//
//            when(userRepository.save(existingUser)).thenReturn(updatedUser);
//            when(userMapper.toResponse(updatedUser)).thenReturn(updatedUserResponse);
//
//            UserResponse response = userAdminService.updateUser(1L, updateRequest);
//
//            assertEquals("newUsername", response.username());
//            assertEquals("new@email.com", response.email());
//            assertEquals("Old Name", response.name());
//>>>>>>> dev
        }
    }
}
