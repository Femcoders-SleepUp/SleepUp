package com.SleepUp.SU.reservation.admin;


import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationAdminServiceImpl implements ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EntityUtil entityUtil;
}


