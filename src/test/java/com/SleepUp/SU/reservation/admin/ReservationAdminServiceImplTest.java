package com.SleepUp.SU.reservation.admin;

import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.ReservationNotFoundByIdException;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.EntityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ReservationAdminServiceImplTest {


    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private EntityUtil entityUtil;

    @InjectMocks
    private ReservationAdminServiceImpl reservationAdminServiceImpl;

    private final User dummyUser = new User();
    private final Long accommodationId = 55L;

    private ReservationResponseSummary response1;
    private ReservationResponseSummary response2;


    @BeforeEach
    void setUp() {
        response1 = new ReservationResponseSummary(
                1L,
                "alice",
                2,
                "Beach House",
                LocalDate.of(2025, 9, 25),
                LocalDate.of(2025, 9, 30),
                BookingStatus.CONFIRMED,
                BigDecimal.valueOf(100)
        );
        response2 = new ReservationResponseSummary(
                2L,
                "bob",
                4,
                "Mountain Cabin",
                LocalDate.of(2025, 10, 5),
                LocalDate.of(2025, 10, 12),
                BookingStatus.PENDING,
                BigDecimal.valueOf(100)
        );
    }

    private Reservation createTestReservation() {
        Reservation reservation = new Reservation();
        reservation.setCheckInDate(LocalDate.now().plusDays(1));
        reservation.setCheckOutDate(LocalDate.now().plusDays(3));
        reservation.setGuestNumber(2);
        reservation.setBookingStatus(BookingStatus.PENDING);
        reservation.setEmailSent(false);
        return reservation;
    }

    @Nested
    class getAllReservationsToAdminReservationTest{
        @Test
        void getAllReservationsToAdmin_shouldReturnAllReservations() {
           Reservation reservation1 = new Reservation();
           Reservation reservation2 = new Reservation();

            List<Reservation> reservations = List.of(reservation1, reservation2);
            when(reservationRepository.findAll()).thenReturn(reservations);
            when(reservationMapper.toSummary(any(Reservation.class)))
                    .thenReturn(mock(ReservationResponseSummary.class));

            List<ReservationResponseSummary> result = reservationAdminServiceImpl.getAllReservations();
            assertThat(result).hasSize(2);
            verify(reservationRepository).findAll();
            verify(reservationMapper, times(2)).toSummary(any(Reservation.class));
        }
    }

    @Nested
    class DeleteReservationByAdmin {

        @Test
        void deleteReservationByAdmin_validReservation_shouldDeleteSuccessfully() {
            Long reservationId = 1L;
            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);

            when(reservationRepository.existsById(reservationId)).thenReturn(true);
            doNothing().when(reservationRepository).deleteById(reservationId);

            assertDoesNotThrow(() -> reservationAdminServiceImpl.deleteReservationByAdmin(reservationId));
            verify(reservationRepository).deleteById(reservationId);
            verify(reservationRepository).existsById(reservationId);
        }

        @Test
        void deleteReservationByAdmin_reservationNotFound_shouldThrowException() {
            Long reservationId = 999L;

            when(reservationRepository.existsById(reservationId)).thenReturn(false);

            ReservationNotFoundByIdException exception = assertThrows(ReservationNotFoundByIdException.class,
                    () -> reservationAdminServiceImpl.deleteReservationByAdmin(reservationId));

            assertEquals("Reservation with id '" +reservationId + "' not found", exception.getMessage());
            verify(reservationRepository).existsById(reservationId);
        }
    }
}