package com.SleepUp.SU.user.user;

import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserMapper;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Nested
    class getLoggedUser{

        @Test
        void when_getLoggesUser_return_loggedUser(){
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);
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

    @Nested
    class updateLoggedUser {

        @Test
        void when_updateLoggedUser_with_new_data_then_return_updated_response() {
            User user = new User(99L, "oldUsername", "oldName", "old@email.com", "oldPassword", Role.USER, null, null);
            UserRequest request = new UserRequest("newUsername", "","new@email.com", "newPassword");

            User updatedUser = new User(99L, "newUsername", "oldName", "new@email.com", "encodedPassword", Role.USER, null, null);
            UserResponse expectedResponse = new UserResponse(99L, "newUsername", "oldName", "new@email.com", Role.USER);

            when(userServiceHelper.findById(99L)).thenReturn(user);
            doAnswer(invocation -> {
                user.setUsername("newUsername");
                user.setEmail("new@email.com");
                user.setPassword("encodedPassword");
                return null;
            }).when(userServiceHelper).updateUserData(request, user);
            when(userMapper.toResponse(user)).thenReturn(expectedResponse);

            UserResponse response = userUserService.updateLoggedUser(request, 99L);

            assertEquals("newUsername", response.username());
            assertEquals("new@email.com", response.email());
            assertEquals("oldName", response.name());
        }

        @Test
        void when_updateLoggedUser_with_empty_request_then_return_same_response() {
            User user = new User(99L, "sameUsername", "sameName", "same@email.com", "samePassword", Role.USER, null, null);
            UserRequest request = new UserRequest("", "", "", "");

            UserResponse expectedResponse = new UserResponse(99L, "sameUsername", "sameName", "same@email.com", Role.USER);

            when(userServiceHelper.findById(99L)).thenReturn(user);
            doAnswer(invocation -> {
                return null;
            }).when(userServiceHelper).updateUserData(request, user);
            when(userMapper.toResponse(user)).thenReturn(expectedResponse);

            UserResponse response = userUserService.updateLoggedUser(request, 99L);

            assertEquals("sameUsername", response.username());
            assertEquals("same@email.com", response.email());
            assertEquals("sameName", response.name());
        }

        @Test
        void when_updateLoggedUser_user_not_found_then_throw_exception() {
            UserRequest request = new UserRequest("any", "any", "any", "any");

            when(userServiceHelper.findById(99L))
                    .thenThrow(new IllegalArgumentException("User not found"));

            assertThrows(IllegalArgumentException.class, () -> {
                userUserService.updateLoggedUser(request, 99L);
            });
        }
    }

    @Nested
    class deleteLoggedUser {
        @Test
        void when_user_exists_then_delete_successfully() {
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);

            when(userServiceHelper.findById(99L)).thenReturn(user);

            when(accommodationRepository.findByManagedBy_Id(99L)).thenReturn(List.of());
            when(reservationRepository.findByUser_Id(99L)).thenReturn(List.of());

            userUserService.deleteMyUser(99L);

            verify(userRepository).deleteById(99L);
            verify(accommodationRepository, never()).saveAll(anyList());
            verify(reservationRepository, never()).saveAll(anyList());
        }

    }

}
