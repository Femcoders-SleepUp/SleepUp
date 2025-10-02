package com.SleepUp.SU.user.user;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUserServiceImpl implements UserUserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EntityUtil mapperUtil;
    private final UserServiceHelper userServiceHelper;
    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public UserResponse getLoggedUser(Long id){
        return userMapper.toResponse(userServiceHelper.getUserEntityById(id));
    }

    @Override
    public UserResponse updateLoggedUser(UserRequest userRequest, Long id){
        User user = userServiceHelper.getUserEntityById(id);
        userServiceHelper.updateUserData(userRequest, user);
        return (userMapper.toResponse(user));
    }

    @Override
    @Transactional
    public void deleteMyUser(Long id){
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

        userRepository.deleteById(userServiceHelper.getUserEntityById(id).getId());
    }
}
