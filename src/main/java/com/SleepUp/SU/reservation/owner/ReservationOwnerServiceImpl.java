package com.SleepUp.SU.reservation.owner;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationOwnerServiceImpl implements ReservationOwnerService{

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationServiceHelper reservationServiceHelper;
    private final EntityUtil entityUtil;
    private final EmailServiceHelper emailServiceHelper;


    @Override
    public List<ReservationResponseSummary> getAllReservationsOnMyAccommodation(User user, Long id){
        List<Reservation> allReservationsByAccommodation = reservationRepository.findByAccommodationId(id);
        if (allReservationsByAccommodation.isEmpty()){throw new RuntimeException("Empty list");}
        return entityUtil.mapEntitiesToDTOs(allReservationsByAccommodation,reservationMapper::toSummary);
    }

    @Override
    @Transactional
    public ReservationResponseDetail updateStatus(Long id, ReservationAuthRequest reservationAuthRequest){
        Reservation isExisting = reservationServiceHelper.getReservationEntityById(id);
        isExisting.setBookingStatus(reservationAuthRequest.bookingStatus());
        long days = ChronoUnit.DAYS.between(isExisting.getCheckInDate(), isExisting.getCheckOutDate());

        double amount = days * isExisting.getAccommodation().getPrice() ;

        if(reservationServiceHelper.validateReservationAccommodationLessThanOneYear(isExisting.getAccommodation().getId(), isExisting.getAccommodation().getManagedBy().getId())){
            amount = amount - amount*0.20;
        }

        if (reservationAuthRequest.bookingStatus().equals(BookingStatus.CONFIRMED)){emailServiceHelper.sendReservationConfirmationEmail(isExisting.getUser(), isExisting.getAccommodation(), isExisting, amount);}
        if (reservationAuthRequest.bookingStatus().equals(BookingStatus.CANCELLED)){emailServiceHelper.sendCancellationByOwnerNotificationEmail(isExisting.getUser(), isExisting.getAccommodation(), isExisting);}

        return reservationMapper.toDetail(isExisting);
    }

    @Override
    public ReservationResponseDetail getReservationById(Long id){
        Reservation isExisting = reservationServiceHelper.getReservationEntityById(id);
        return reservationMapper.toDetail(isExisting);
    }

}
