package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.dto.*;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ReservationMapperImpl.class)
public class ReservationMapperTest {

    @Autowired
    private ReservationMapper reservationMapper;

    private Reservation reservation;
    private ReservationRequest reservationRequest;
    private BookingStatus bookingStatus;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
                .id(1L)
                .guestNumber(2)
                .checkInDate(LocalDate.of(2025, 9, 20))
                .checkOutDate(LocalDate.of(2025, 9, 25))
                .bookingStatus(BookingStatus.CONFIRMED)
                .emailSent(true)
                .createdDate(LocalDateTime.now())
                .build();

        reservationRequest = new ReservationRequest(3,
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 5));

        bookingStatus = BookingStatus.PENDING;
    }

    @Test
    public void testToSummaryMapping() {
        ReservationResponseSummary summary = reservationMapper.toSummary(reservation);
        assertNotNull(summary);
        assertEquals(reservation.getId(), summary.id());
        assertEquals(reservation.getGuestNumber(), summary.guestNumber());
        assertEquals(reservation.getCheckInDate(), summary.checkInDate());
        assertEquals(reservation.getCheckOutDate(), summary.checkOutDate());
        assertEquals(reservation.getBookingStatus(), summary.bookingStatus());
        assertEquals(reservation.getEmailSent(), summary.emailSent());
        assertEquals(reservation.getCreatedDate(), summary.createdDate());
    }

    @Test
    public void testToDetailMapping() {
        ReservationResponseDetail detail = reservationMapper.toDetail(reservation);
        assertNotNull(detail);
        assertEquals(reservation.getId(), detail.id());
        assertEquals(reservation.getGuestNumber(), detail.guestNumber());
        assertEquals(reservation.getCheckInDate(), detail.checkInDate());
        assertEquals(reservation.getCheckOutDate(), detail.checkOutDate());
        assertEquals(reservation.getBookingStatus(), detail.bookingStatus());
        assertEquals(reservation.getEmailSent(), detail.emailSent());
        assertEquals(reservation.getCreatedDate(), detail.createdDate());
    }

    @Test
    public void testToEntityMapping() {
        Reservation entity = reservationMapper.toEntity(reservationRequest, bookingStatus, new User(),new Accommodation(),false);
        assertNotNull(entity);
        assertEquals(reservationRequest.guestNumber(), entity.getGuestNumber());
        assertEquals(reservationRequest.checkInDate(), entity.getCheckInDate());
        assertEquals(reservationRequest.checkOutDate(), entity.getCheckOutDate());
        assertEquals(bookingStatus, entity.getBookingStatus());
    }
}
