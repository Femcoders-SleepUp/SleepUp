package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.admin.UserAdminService;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserUserServiceTest {

    @InjectMocks
    private UserUserService userUserService;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Nested
    class getLoggedUser{

        @Test
        void when_getLoggesUser_return_loggedUser(){
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER);
            UserResponse userResponse = new UserResponse(99L,"usernameTest", "nameTest", "email@test.com", Role.USER);

            when(userServiceHelper.findById(99L)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);
            UserResponse response = userUserService.getLoggedUser(99L);

            assertEquals("usernameTest", response.username());
            assertEquals("nameTest", response.name());
            assertEquals("email@test.com", response.email());

        }

        @Test
        void when_getLoggedUser_throw_exception() {
            when(userServiceHelper.findById(99L))
                    .thenThrow(new IllegalArgumentException("Username by id does not exist"));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userUserService.getLoggedUser(99L)
            );

            assertEquals("Username by id does not exist", exception.getMessage());

        }
    }

}
