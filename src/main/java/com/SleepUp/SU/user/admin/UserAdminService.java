package com.SleepUp.SU.user.admin;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.Reservation;
import com.SleepUp.SU.reservation.ReservationRepository;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.dto.UserRequestAdmin;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityUtil mapperUtil;
    private final UserServiceHelper userServiceHelper;
    private final PasswordEncoder passwordEncoder;
    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;

    public List<UserResponse> getAllUsers() {
        return mapperUtil.mapEntitiesToDTOs(userRepository.findAll(), userMapper::toResponse);
    }

    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(userServiceHelper.findById(userId));
    }

    public UserResponse createUser(UserRequestAdmin userRequestAdmin) {
        if (userRepository.findByUsername(userRequestAdmin.username()).isPresent()) {
            throw new RuntimeException("Username already exists " + userRequestAdmin.username());
        }

        String encodedPassword = passwordEncoder.encode(userRequestAdmin.password());
        User user = userMapper.toEntityAdmin(userRequestAdmin, encodedPassword);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserRequestAdmin userRequestAdmin) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        userServiceHelper.updateUserDataAdmin(userRequestAdmin, existingUser);

        existingUser.setRole(userRequestAdmin.role());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUserById(Long id){
        if (id.equals(1L)) {
            throw new IllegalArgumentException("Cannot delete replacement user with ID 1");
        }

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User with id " + id + " does not exist");
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
            reservationRepository.saveAll(reservationList);
        }

        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userServiceHelper.findByUsername(username);
        return new CustomUserDetails(user);
    }
}
