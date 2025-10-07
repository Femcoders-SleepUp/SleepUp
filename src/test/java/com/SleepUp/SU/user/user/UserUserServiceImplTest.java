package com.SleepUp.SU.user.user;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserUserServiceImplTest {

    @InjectMocks
    private UserUserServiceImpl userUserServiceImpl;

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
        void getLoggedUser_whenUserExists_shouldReturnLoggedUser(){
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);
            UserResponse userResponse = new UserResponse(99L,"usernameTest", "nameTest", "email@test.com", Role.USER);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);
            UserResponse response = userUserServiceImpl.getLoggedUser(99L);

            assertEquals("usernameTest", response.username());
            assertEquals("nameTest", response.name());
            assertEquals("email@test.com", response.email());
        }

        @Test
        void getLoggedUser_whenUserNotFound_shouldThrowException() {
            when(userServiceHelper.getUserEntityById(99L))
                    .thenThrow(new IllegalArgumentException("Username by id does not exist"));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userUserServiceImpl.getLoggedUser(99L)
            );

            assertEquals("Username by id does not exist", exception.getMessage());
        }
    }

    @Nested
    class updateLoggedUser {

        @Test
        void updateLoggedUser_whenValidRequest_shouldReturnUpdatedUser() {
            User user = new User(99L, "oldUsername", "oldName", "old@email.com", "oldPassword", Role.USER, null, null);
            UserRequest request = new UserRequest("newUsername", "","new@email.com", "newPassword");

            User updatedUser = new User(99L, "newUsername", "oldName", "new@email.com", "encodedPassword", Role.USER, null, null);
            UserResponse expectedResponse = new UserResponse(99L, "newUsername", "oldName", "new@email.com", Role.USER);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            doAnswer(invocation -> {
                user.setUsername("newUsername");
                user.setEmail("new@email.com");
                user.setPassword("encodedPassword");
                return null;
            }).when(userServiceHelper).updateUserData(request, user);
            when(userMapper.toResponse(user)).thenReturn(expectedResponse);

            UserResponse response = userUserServiceImpl.updateLoggedUser(request, 99L);

            assertEquals("newUsername", response.username());
            assertEquals("new@email.com", response.email());
            assertEquals("oldName", response.name());
        }

        @Test
        void updateLoggedUser_whenEmptyRequest_shouldReturnSameUser() {
            User user = new User(99L, "sameUsername", "sameName", "same@email.com", "samePassword", Role.USER, null, null);
            UserRequest request = new UserRequest("", "", "", "");

            UserResponse expectedResponse = new UserResponse(99L, "sameUsername", "sameName", "same@email.com", Role.USER);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            doAnswer(invocation -> {
                return null;
            }).when(userServiceHelper).updateUserData(request, user);
            when(userMapper.toResponse(user)).thenReturn(expectedResponse);

            UserResponse response = userUserServiceImpl.updateLoggedUser(request, 99L);

            assertEquals("sameUsername", response.username());
            assertEquals("same@email.com", response.email());
            assertEquals("sameName", response.name());
        }

        @Test
        void updateLoggedUser_whenUserNotFound_shouldThrowException() {
            UserRequest request = new UserRequest("any", "any", "any", "any");

            when(userServiceHelper.getUserEntityById(99L))
                    .thenThrow(new IllegalArgumentException("User not found"));

            assertThrows(IllegalArgumentException.class, () -> {
                userUserServiceImpl.updateLoggedUser(request, 99L);
            });
        }
    }

    @Nested
    class deleteLoggedUser {

        @Test
        void deleteLoggedUser_whenUserExists_shouldDeleteSuccessfully() {
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);
            User user1 = new User(1L,"usernameTest1", "nameTest1", "email1@test.com", "testPassword", Role.USER, null, null);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

            when(accommodationRepository.findByManagedBy_Id(99L)).thenReturn(List.of());
            when(reservationRepository.findByUser_Id(99L)).thenReturn(List.of());

            userUserServiceImpl.deleteMyUser(99L);

            verify(userRepository).deleteById(99L);
            verify(accommodationRepository, never()).saveAll(anyList());
            verify(reservationRepository, never()).saveAll(anyList());
        }

        @Test
        void deleteLoggedUser_whenReplacementUserNotFound_shouldThrowException() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userUserServiceImpl.deleteMyUser(99L));
            assertEquals("Replacement user with ID 1 not found", ex.getMessage());

            verify(userRepository, never()).deleteById(anyLong());
        }

        @Test
        void deleteLoggedUser_whenAccommodationsExist_shouldReassignAndSave() {
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);
            User replacement = new User(1L,"replacement", "replacement", "replacement@test.com", "pass", Role.USER, null, null);
            Accommodation acc = new Accommodation();
            acc.setManagedBy(user);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            when(userRepository.findById(1L)).thenReturn(Optional.of(replacement));
            when(accommodationRepository.findByManagedBy_Id(99L)).thenReturn(List.of(acc));
            when(reservationRepository.findByUser_Id(99L)).thenReturn(List.of());

            userUserServiceImpl.deleteMyUser(99L);

            verify(reservationRepository, never()).saveAll(anyList());
            verify(userRepository).deleteById(99L);
        }

        @Test
        void deleteLoggedUser_whenReservationsExist_shouldReassignSetCancelledAndSave() {
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);
            User replacement = new User(1L,"replacement", "replacement", "replacement@test.com", "pass", Role.USER, null, null);
            Reservation reservation = new Reservation();
            reservation.setUser(user);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            when(userRepository.findById(1L)).thenReturn(Optional.of(replacement));
            when(accommodationRepository.findByManagedBy_Id(99L)).thenReturn(List.of());
            when(reservationRepository.findByUser_Id(99L)).thenReturn(List.of(reservation));

            userUserServiceImpl.deleteMyUser(99L);

            verify(accommodationRepository, never()).saveAll(anyList());
            verify(userRepository).deleteById(99L);
        }

        @Test
        void deleteLoggedUser_whenBothAccommodationsAndReservationsExist_shouldProcessBoth() {
            User user = new User(99L,"usernameTest", "nameTest", "email@test.com", "testPassword", Role.USER, null, null);
            User replacement = new User(1L,"replacement", "replacement", "replacement@test.com", "pass", Role.USER, null, null);

            Accommodation acc = new Accommodation();
            acc.setManagedBy(user);
            Reservation reservation = new Reservation();
            reservation.setUser(user);

            when(userServiceHelper.getUserEntityById(99L)).thenReturn(user);
            when(userRepository.findById(1L)).thenReturn(Optional.of(replacement));
            when(accommodationRepository.findByManagedBy_Id(99L)).thenReturn(List.of(acc));
            when(reservationRepository.findByUser_Id(99L)).thenReturn(List.of(reservation));

            userUserServiceImpl.deleteMyUser(99L);

            verify(userRepository).deleteById(99L);
        }
    }
}