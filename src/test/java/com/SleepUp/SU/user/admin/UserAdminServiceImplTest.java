package com.SleepUp.SU.user.admin;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByIdException;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByUsernameException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceImplTest {

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EntityUtil mapperUtil;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;

    @Nested
    class GetAllUsersTests {
        @Test
        void getAllUsers_shouldReturnMappedUserResponses() {
            List<User> users = List.of(
                    new User(),
                    new User()
            );

            List<UserResponse> responses = List.of(
                    new UserResponse(1L, "user1", "Name1", "email1@test.com", Role.USER),
                    new UserResponse(2L, "user2", "Name2", "email2@test.com", Role.ADMIN)
            );

            when(userRepository.findAll()).thenReturn(users);
            when(mapperUtil.mapEntitiesToDTOs(anyList(), any())).thenReturn((List) responses);

            List<UserResponse> result = userAdminService.getAllUsers();

            assertEquals(2, result.size());
            assertEquals("user1", result.getFirst().username());
            verify(userRepository).findAll();
            verify(mapperUtil).mapEntitiesToDTOs(anyList(), any());
        }
    }

    @Nested
    class GetUserByIdTests {
        @Test
        void getUserById_existingUser_shouldReturnResponse() {
            User user = new User();
            user.setId(1L);
            UserResponse userResponse = new UserResponse(1L, "user", "Name", "email@test.com", Role.USER);

            when(userServiceHelper.getUserEntityById(1L)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userAdminService.getUserById(1L);

            assertEquals(1L, result.id());
            assertEquals("user", result.username());

            verify(userServiceHelper).getUserEntityById(1L);
            verify(userMapper).toResponse(user);
        }
    }

    @Nested
    class CreateUserTests {
        @Test
        void createUser_shouldCreateSendEmailAndReturnResponse() {
            UserRequest request = new UserRequest("user", "Name", "email@test.com", "password");
            Role role = Role.USER;
            User savedUser = new User();
            UserResponse response = new UserResponse(1L,"user","Name","email@test.com",role);

            when(userServiceHelper.createUser(request, role)).thenReturn(savedUser);
            doNothing().when(emailService).sendWelcomeEmail(savedUser);
            when(userMapper.toResponse(savedUser)).thenReturn(response);

            UserResponse result = userAdminService.createUser(request, role);

            verify(userServiceHelper).createUser(request, role);
            verify(emailService).sendWelcomeEmail(savedUser);
            verify(userMapper).toResponse(savedUser);

            assertEquals("user", result.username());
        }
    }

    @Nested
    class UpdateUserTests {
        @Test
        void updateUser_existingUser_shouldReturnUpdatedUserResponse() {
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
            when(userMapper.toResponse(updatedUser)).thenReturn(userResponse);

            UserResponse response = userAdminService.updateUser(1L, request);

            verify(userServiceHelper).getUserEntityById(1L);
            verify(userServiceHelper).updateUserDataAdmin(eq(request), any(User.class));
            verify(userMapper).toResponse(updatedUser);

            assertEquals("updatedUser", response.username());
            assertEquals(Role.ADMIN, response.role());
        }

        @Test
        void updateUser_userNotFound_shouldThrow() {
            UserRequestAdmin request = UserRequestAdmin.builder().build();

            when(userServiceHelper.getUserEntityById(1L)).thenThrow(new UserNotFoundByIdException(1L));

            assertThrows(UserNotFoundByIdException.class, () -> userAdminService.updateUser(1L, request));
        }
    }

    @Nested
    class DeleteUserByIdTests {

        @Test
        void deleteUserById_cannotDeleteReplacementUser() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> userAdminService.deleteUserById(1L));
            assertEquals("Cannot delete replacement user with ID 1", exception.getMessage());
        }

        @Test
        void deleteUserById_userNotExists_shouldThrow() {
            when(userRepository.existsById(2L)).thenReturn(false);

            assertThrows(UserNotFoundByIdException.class, () -> userAdminService.deleteUserById(2L));
        }

        @Test
        void deleteUserById_replacementUserNotFound_shouldThrow() {
            when(userRepository.existsById(3L)).thenReturn(true);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userAdminService.deleteUserById(3L));
            assertEquals("Replacement user with ID 1 not found", ex.getMessage());
        }

        @Test
        void deleteUserById_withAccommodationsAndReservations_shouldReassignAndDelete() {
            long userId = 4L;
            User replacementUser = new User();
            replacementUser.setId(1L);

            Accommodation acc1 = new Accommodation();
            Accommodation acc2 = new Accommodation();
            List<Accommodation> accommodations = List.of(acc1, acc2);

            Reservation res1 = new Reservation();
            Reservation res2 = new Reservation();
            List<Reservation> reservations = List.of(res1, res2);

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(replacementUser));
            when(accommodationRepository.findByManagedBy_Id(userId)).thenReturn(accommodations);
            when(reservationRepository.findByUser_Id(userId)).thenReturn(reservations);

            doNothing().when(userRepository).deleteById(userId);

            userAdminService.deleteUserById(userId);

            assertEquals(replacementUser, acc1.getManagedBy());
            assertEquals(replacementUser, acc2.getManagedBy());

            for (Reservation reservation : reservations) {
                assertEquals(replacementUser, reservation.getUser());
                assertEquals(BookingStatus.CANCELLED, reservation.getBookingStatus());
            }

            verify(accommodationRepository).saveAll(accommodations);
            verify(reservationRepository).saveAll(reservations);
            verify(userRepository).deleteById(userId);
        }

        @Test
        void deleteUserById_noAccommodationsOrReservations_shouldDeleteDirectly() {
            long userId = 5L;
            User replacementUser = new User();

            when(userRepository.existsById(userId)).thenReturn(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(replacementUser));
            when(accommodationRepository.findByManagedBy_Id(userId)).thenReturn(new ArrayList<>());
            when(reservationRepository.findByUser_Id(userId)).thenReturn(new ArrayList<>());

            doNothing().when(userRepository).deleteById(userId);

            userAdminService.deleteUserById(userId);

            verify(accommodationRepository, never()).saveAll(any());
            verify(reservationRepository, never()).saveAll(any());
            verify(userRepository).deleteById(userId);
        }
    }

    @Nested
    class LoadUserByUsernameTests {

        @Test
        void loadUserByUsername_existingUser_shouldReturnCustomUserDetails() {
            User user = new User();
            user.setUsername("testUser");

            when(userServiceHelper.getUserEntityByUsername("testUser")).thenReturn(user);

            CustomUserDetails details = (CustomUserDetails) userAdminService.loadUserByUsername("testUser");

            assertEquals("testUser", details.getUsername());
            verify(userServiceHelper).getUserEntityByUsername("testUser");
        }

        @Test
        void loadUserByUsername_userNotFound_shouldThrowUsernameNotFoundException() {
            when(userServiceHelper.getUserEntityByUsername("unknownUser"))
                    .thenThrow(new UserNotFoundByUsernameException("unknownUser"));

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                    () -> userAdminService.loadUserByUsername("unknownUser"));
            assertTrue(ex.getMessage().contains("unknownUser"));
            verify(userServiceHelper).getUserEntityByUsername("unknownUser");
        }
    }
}