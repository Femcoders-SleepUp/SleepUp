package com.SleepUp.SU.user.admin;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import com.SleepUp.SU.utils.exceptions.UserNotFoundByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityUtil mapperUtil;
    private final UserServiceHelper userServiceHelper;
    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;
    private final EmailServiceHelper emailServiceHelper;

    @Override
    public List<UserResponse> getAllUsers() {
        return mapperUtil.mapEntitiesToDTOs(userRepository.findAll(), userMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(userServiceHelper.getUserEntityById(userId));
    }

    @Override
    public UserResponse createUser(UserRequest userRequest, Role role) {
        User savedUser = userServiceHelper.createUser(userRequest, role);
        emailServiceHelper.sendWelcomeEmail(userRequest, savedUser);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserRequestAdmin userRequestAdmin) {;
        User user = userServiceHelper.getUserEntityById(userId);
        User updatedUser = userServiceHelper.updateUserDataAdmin(userRequestAdmin, user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id){
        if (id.equals(1L)) {
            throw new IllegalArgumentException("Cannot delete replacement user with ID 1");
        }

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundByIdException(id);
        }

        User replacementUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Replacement user with ID 1 not found"));

        List<Accommodation> accommodationList = accommodationRepository.findByManagedBy_Id(id);
        if (!accommodationList.isEmpty()) {
            accommodationList.forEach(accommodation -> accommodation.setManagedBy(replacementUser));
            accommodationRepository.saveAll(accommodationList);
        }

        List<Reservation> reservationList = reservationRepository.findByUser_Id(id);
        if (!reservationList.isEmpty()) {
            reservationList.forEach(reservation -> reservation.setUser(replacementUser));
            reservationList.forEach(reservation -> reservation.setBookingStatus(BookingStatus.CANCELLED));
            reservationRepository.saveAll(reservationList);
        }

        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userServiceHelper.getUserEntityByUsername(username);
        return new CustomUserDetails(user);
    }
}
