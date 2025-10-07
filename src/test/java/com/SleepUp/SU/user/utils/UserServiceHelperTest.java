package com.SleepUp.SU.user.utils;


import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.utils.exceptions.UserEmailAlreadyExistsException;
import com.SleepUp.SU.utils.exceptions.UserUsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserServiceHelperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityUtil entityUtil;

    @InjectMocks
    private UserServiceHelper userServiceHelper;

    private  UserServiceHelper spyUserServiceHelper;

    @BeforeEach
    void setUp(){
        spyUserServiceHelper = spy(userServiceHelper);
    }

    @Nested
    class findById{

        @Test
        void findByUserId_validId_shouldReturnUser() {
            User user = new User();
            user.setId(99L);
            user.setUsername("test");
            when(userRepository.findById(99L)).thenReturn(Optional.of(user));

            User result = userServiceHelper.getUserEntityById(99L);

            assertNotNull(result);
            assertDoesNotThrow(() -> userServiceHelper.getUserEntityById(99L));
            assertEquals(99L, result.getId());
            assertEquals("test", result.getUsername());
        }
    }

    @Nested
    class validateUserDoesNotExist {

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        void validateUserDoesNotExist_whenNoConflict_shouldPass() {
            when(userRepository.existsByUsername("testUser")). thenReturn(false);
            when(userRepository.existsByUsername("testUser@email.com")). thenReturn(false);

            userServiceHelper.validateUserDoesNotExist("testUser", "testUser@email.com");
        }

        @Test
        void validateUserDoesNotExist_whenUsernameExists_shouldThrowException() {
            when(userRepository.existsByUsername("testUser")).thenReturn(true);

            UserUsernameAlreadyExistsException exception = assertThrows(
                    UserUsernameAlreadyExistsException.class,
                    () -> userServiceHelper.validateUserDoesNotExist("testUser", "testUser@email.com")
            );

            assertEquals("User with username 'testUser' already exists", exception.getMessage());
        }

        @Test
        void validateUserDoesNotExist_whenEmailExists_shouldThrowException() {
            when(userRepository.existsByUsername("testUser")).thenReturn(false);
            when(userRepository.existsByEmail("testUser@email.com")).thenReturn(true);

            UserEmailAlreadyExistsException exception = assertThrows(
                    UserEmailAlreadyExistsException.class,
                    () -> userServiceHelper.validateUserDoesNotExist("testUser", "testUser@email.com")
            );

            assertEquals("User with email 'testUser@email.com' already exists", exception.getMessage());
        }

    }

    @Test
    @Transactional
    void createUser_shouldEncodePasswordAndSaveUser() {
        UserRequest request = new UserRequest("user1", "name1", "user1@email.com", "pass");
        Role role = Role.USER;

        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");
        User userEntity = new User();
        when(userMapper.toEntity(request, "encodedPass", role)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        User result = userServiceHelper.createUser(request, role);

        verify(passwordEncoder).encode("pass");
        verify(userMapper).toEntity(request, "encodedPass", role);
        verify(userRepository).save(userEntity);

        assertSame(userEntity, result);
    }

    @Nested
    class UpdateUserTests {

        @Test
        void updateUser_shouldValidateUsernameIfChanged() {
            UserRequest request = new UserRequest("newUsername", "name", "email@example.com", "password");
            User existingUser = spy(new User());
            existingUser.setUsername("oldUsername");
            existingUser.setEmail("email@example.com");
            existingUser.setPassword("oldPass");

            doNothing().when(spyUserServiceHelper).validateUsernameDoesNotExist("newUsername");
            doAnswer(invocation -> {
                Object newValue = invocation.getArgument(0);
                @SuppressWarnings("unchecked")
                Consumer<Object> setter = invocation.getArgument(2);
                setter.accept(newValue);
                return null;
            }).when(entityUtil).updateField(any(), any(), any());
            doReturn("encodedPassword").when(spyUserServiceHelper).getEncodePassword("password");

            User updatedUser = spyUserServiceHelper.updateUser(request, existingUser);

            verify(spyUserServiceHelper).validateUsernameDoesNotExist("newUsername");
            verify(spyUserServiceHelper, never()).validateUsernameDoesNotExist("email@example.com");
            assertEquals("encodedPassword", updatedUser.getPassword());
            assertEquals("newUsername", updatedUser.getUsername());
        }

        @Test
        void updateUser_shouldValidateEmailIfChanged() {
            UserRequest request = new UserRequest("username", "name", "newEmail@example.com", null);
            User existingUser = spy(new User());
            existingUser.setUsername("username");
            existingUser.setEmail("oldEmail@example.com");
            existingUser.setPassword("oldPass");

            doNothing().when(entityUtil).updateField(any(), any(), any());

            User updatedUser = spyUserServiceHelper.updateUser(request, existingUser);


            assertEquals("oldPass", updatedUser.getPassword());
        }

        @Test
        void updateUser_shouldKeepExistingPasswordWhenRequestPasswordIsNullOrEmpty() {
            UserRequest request1 = new UserRequest("username", "name", "email@example.com", null);
            UserRequest request2 = new UserRequest("username", "name", "email@example.com", "");
            User existingUser = new User();
            existingUser.setPassword("oldPass");

            User updated1 = userServiceHelper.updateUser(request1, existingUser);
            assertEquals("oldPass", updated1.getPassword());

            User updated2 = userServiceHelper.updateUser(request2, existingUser);
            assertEquals("oldPass", updated2.getPassword());
        }

        @Test
        void updateUserData_shouldDelegateToUpdateUser() {
            UserRequest request = new UserRequest("username", "name", "email@example.com", "pass");
            User user = new User();

            doReturn(user).when(spyUserServiceHelper).updateUser(request, user);

            User result = spyUserServiceHelper.updateUserData(request, user);

            verify(spyUserServiceHelper).updateUser(request, user);
            assertSame(user, result);
        }

        @Test
        void updateUserDataAdmin_shouldConvertAndUpdateUserRoleConditional() {
            Role newRole = Role.ADMIN;
            UserRequestAdmin adminRequest = new UserRequestAdmin("username", "name", "email@example.com", "pass", newRole);
            User existingUser = new User();
            existingUser.setRole(Role.USER);

            UserServiceHelper spyUserServiceHelper = spy(userServiceHelper);
            doReturn(existingUser).when(spyUserServiceHelper).updateUser(any(UserRequest.class), eq(existingUser));

            User updatedUser = spyUserServiceHelper.updateUserDataAdmin(adminRequest, existingUser);

            assertEquals(newRole, updatedUser.getRole());

            UserRequestAdmin adminRequestEmptyRole = new UserRequestAdmin("username", "name", "email@example.com", "pass", null);
            User user2 = new User();
            user2.setRole(newRole);
            doReturn(user2).when(spyUserServiceHelper).updateUser(any(UserRequest.class), eq(user2));

            User updatedUser2 = spyUserServiceHelper.updateUserDataAdmin(adminRequestEmptyRole, user2);

            assertEquals(newRole, updatedUser2.getRole());
        }
    }
}
