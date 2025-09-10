package com.SleepUp.SU.user.utils;

import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceHelperTest {
    @InjectMocks
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Nested
    class getUserLogin {

        @Test
        void when_getUserLogin_return_user() {
            User user = new User();
            user.setId(99L);
            user.setUsername("test");
            when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));

            Optional<User> result = userServiceHelper.getUserLogin("test");
            User userResponse = result.get();
            assertNotNull(result);
            assertDoesNotThrow(() ->userServiceHelper.getUserLogin("test"));
            assertEquals(99L, userResponse.getId());
            assertEquals("test", userResponse.getUsername());
        }

        @Test
        void when_getUserLogin_throw_UsernameNotFoundException() {
            when(userRepository.findByUsername("test")).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> userServiceHelper.getUserLogin("test"));
        }
    }

}
