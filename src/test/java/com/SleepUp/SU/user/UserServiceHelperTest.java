package com.SleepUp.SU.user;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceHelperTest {

    @InjectMocks
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Nested
    class findById{

        @Test
        void when_checkUserId_return_user() {
            User user = new User();
            user.setId(99L);
            user.setUsername("test");
            when(userRepository.findById(99L)).thenReturn(Optional.of(user));

            User result = userServiceHelper.findById(99L);

            assertNotNull(result);
            assertDoesNotThrow(() ->userServiceHelper.findById(99L));
            assertEquals(99L, result.getId());
            assertEquals("test", result.getUsername());
        }
    }

    @Nested
    class validateUserDoesNotExist {

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        void when_validateUserDoesNotExist_then_noExceptionThrow() {
            when(userRepository.existsByUsername("testUser")). thenReturn(false);
            when(userRepository.existsByUsername("testUser@email.com")). thenReturn(false);

            userServiceHelper.validateUserDoesNotExist("testUser", "testUser@email.com");
        }

        @Test
        void when_username_exists_then_throw_exception() {
            when(userRepository.existsByUsername("testUser")).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userServiceHelper.validateUserDoesNotExist("testUser", "testUser@email.com")
            );

            assertEquals("already exists username", exception.getMessage());
        }

        @Test
        void when_email_exists_then_throw_exception() {
            when(userRepository.existsByUsername("testUser")).thenReturn(false);
            when(userRepository.existsByEmail("testUser@email.com")).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userServiceHelper.validateUserDoesNotExist("testUser", "testUser@email.com")
            );

            assertEquals("already exists email", exception.getMessage());
        }


    }
}
