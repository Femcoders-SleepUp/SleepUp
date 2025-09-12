package com.SleepUp.SU.user;

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
//
//    @Mock
//    private UserMapperDto userMapperDto;
//
//
//    @Nested
//    class LoginUserTest {
//
//        @Test
//        void should_loginExistingUser_fromRequest() {
//            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");
//            User userSaved = new User();
//            userSaved.setId(1L);
//            userSaved.setUsername("userTest");
//            userSaved.setName("nameTest");
//            userSaved.setEmail("usertest@test.com");
//            userSaved.setPassword("password123");
//            userSaved.setRoles(Set.of(Role.USER));
//
//            when(userServiceHelper.getUserLogin("userTest")).thenReturn(Optional.of(userSaved));
//
//            List<GrantedAuthority> authorities = List.of(
//                    new SimpleGrantedAuthority("ROLE_USER")
//            );
//
//            UserDetails userLogExpected = new org.springframework.security.core.userdetails.User(
//                    "userTest",
//                    "usertest@test.com",
//                    true,
//                    true,
//                    true,
//                    true,
//                    authorities);
//
//            UserDetails userLogResponse = userService.loadUserByUsername("userTest");
//
//            assertEquals(userLogExpected, userLogResponse);
//
//        }
//
//        @Test
//        void should_loginExistingUser_throw_exception() {
//
//            when(userServiceHelper.getUserLogin("userTest"))
//                    .thenThrow(new UsernameNotFoundException("userTest does not exist."));
//
//            assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("userTest"));
//        }
//
//    }
//
//    @Nested
//    class RegisterNewUserTest {
//
//        @Test
//        void should_registerNewUser_fromRequest(){
//            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");
//
//            User mappedUser = new User();
//            mappedUser.setUsername(userRequest.username());
//            mappedUser.setEmail(userRequest.email());
//            mappedUser.setName(userRequest.name());
//            mappedUser.setPassword(userRequest.password());
//
//            doNothing().when(userServiceHelper).validateUserDoesNotExist(userRequest.username(), userRequest.email());
//            when(userMapperDto.toEntity(userRequest)).thenReturn(mappedUser);
//
//            User userSaved = new User();
//            userSaved.setId(1L);
//            userSaved.setUsername("userTest");
//            userSaved.setName("nameTest");
//            userSaved.setEmail("usertest@test.com");
//            userSaved.setPassword("password123");
//            userSaved.setRole(Role.USER);
//
//            when(userRepository.save(any(User.class))).thenReturn(userSaved);
//
//            UserResponse userResponseMock = new UserResponse(
//                    userSaved.getId(),
//                    userSaved.getUsername(),
//                    userSaved.getEmail(),
//                    Collections.singleton(userSaved.getRoles().toString())
//            );
//            when(userMapperDto.fromEntity(userSaved)).thenReturn(userResponseMock);
//
//            UserResponse userResponse = userService.registerUser(userRequest);
//
//            assertEquals("userTest", userResponse.username());
//            assertEquals("usertest@test.com", userResponse.email());
//        }
//
//        @Test
//        void should_registerNewUser_throw_exceptionUsername(){
//
//            User userSaved = new User();
//            userSaved.setId(1L);
//            userSaved.setUsername("userTest");
//            userSaved.setEmail("usertest@test.com");
//            userSaved.setPassword("password123");
//            userSaved.setRoles(Set.of(Role.USER));
//
//            UserRequest userRequest = new UserRequest("userTest", "nameTest",  "usertest@test.com", "password123");
//
//            doThrow(new RuntimeException("UsernameAlreadyExistException"))
//                    .when(userServiceHelper).checkUsername(userRequest.username());
//
//            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(userRequest));
//            assertEquals(new RuntimeException("UsernameAlreadyExistException").getMessage(), exception.getMessage());
//        }
//
//        @Test
//        void should_registerNewUser_throw_exceptionEmail(){
//
//            User userSaved = new User();
//            userSaved.setId(1L);
//            userSaved.setUsername("userTest");
//            userSaved.setEmail("usertest@test.com");
//            userSaved.setPassword("password123");
//            userSaved.setRoles(Set.of(Role.USER));
//
//            UserRequest userRequest = new UserRequest("userTest", "nameTest",  "usertest@test.com", "password123");
//
//            doThrow(new RuntimeException("EmailAlreadyExistException"))
//                    .when(userServiceHelper).checkEmail(userRequest.email());
//
//            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(userRequest));
//            assertEquals(new RuntimeException("EmailAlreadyExistException").getMessage(), exception.getMessage());
//        }
//
//        @Test
//        void should_RegisterNewUser_throw_dataIntegrityViolationException() throws Exception {
//            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");
//            User mappedUser = new User();
//            mappedUser.setUsername(userRequest.username());
//            mappedUser.setEmail(userRequest.email());
//            mappedUser.setName(userRequest.name());
//            mappedUser.setPassword(userRequest.password());
//
//
//            doNothing().when(userServiceHelper).checkUsername(userRequest.username());
//            doNothing().when(userServiceHelper).checkEmail(userRequest.email());
//
//            when(userMapperDto.toEntity(userRequest)).thenReturn(mappedUser);
//            when(userRepository.save(any(User.class)))
//                    .thenThrow(new DataIntegrityViolationException("Username or email already exists"));
//
//            DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> userService.registerUser(userRequest));
//            assertEquals("Username or email already exists", exception.getMessage());
//        }
//    }



}
