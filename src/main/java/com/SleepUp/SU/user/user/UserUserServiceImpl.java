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
        return userMapper.toResponse(userServiceHelper.findById(id));
    }

    @Override
    public UserResponse updateLoggedUser(UserRequest userRequest, Long id){
        User user = userServiceHelper.findById(id);
        userServiceHelper.updateUserData(userRequest, user);
        return (userMapper.toResponse(user));
    }

    @Override
    @Transactional
    public void deleteMyUser(Long id){
        User replacementUser = userServiceHelper.findById(id);
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

        userRepository.deleteById(userServiceHelper.findById(id).getId());
    }
}
