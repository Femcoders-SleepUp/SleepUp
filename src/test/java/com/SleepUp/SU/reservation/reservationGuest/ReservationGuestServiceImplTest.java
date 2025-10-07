package com.SleepUp.SU.reservation.reservationGuest;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.exceptions.ReservationNotFoundByIdException;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import com.SleepUp.SU.utils.email.EmailService;
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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReservationGuestServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private EmailService emailService;

    @Mock
    private EntityUtil entityUtil;

    @InjectMocks
    private ReservationGuestServiceImpl reservationGuestServiceImpl;

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

    @Nested
    class getReservationByIdTest {

        @Test
        void getReservationById_nonExisting_shouldThrow() {
            Long id = 123L;

            when(reservationServiceHelper.getReservationEntityById(id)).thenThrow(new ReservationNotFoundByIdException(id));

            assertThatThrownBy(() ->
                    reservationGuestServiceImpl.getReservationById(id))
                    .isInstanceOf(ReservationNotFoundByIdException.class)
                    .hasMessage("Reservation with id '123' not found");

            verify(reservationMapper, never()).toDetail(any());
        }

        @Test
        void getReservationById_existing_shouldReturnDetail() {
            Long id = 456L;
            Reservation isExisting = new Reservation();

            when(reservationServiceHelper.getReservationEntityById(id)).thenReturn(isExisting);
            ReservationResponseDetail detailDto = mock(ReservationResponseDetail.class);
            when(reservationMapper.toDetail(isExisting))
                    .thenReturn(detailDto);

            ReservationResponseDetail result =
                    reservationGuestServiceImpl.getReservationById(id);

            assertThat(result).isSameAs(detailDto);
            verify(reservationMapper).toDetail(isExisting);
        }
    }

    @Nested
    class CancelReservation {

        @Test
        void cancelReservation_validReservation_shouldReturnCancelledDetail() {
            Long reservationId = 1L;

            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);

            Accommodation testAccommodation = new Accommodation();
            testAccommodation.setId(accommodationId);
            testAccommodation.setName("Accommodation test");

            testReservation.setAccommodation(testAccommodation);

            ReservationResponseDetail expectedResponse = expectedCancelledResponse();

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(testReservation);
            when(reservationRepository.save(testReservation)).thenReturn(testReservation);

            ApiMessageDto result = reservationGuestServiceImpl.cancelReservation(reservationId);

            assertNotNull(result);
        }

        @Test
        void cancelReservation_reservationNotFound_shouldThrowException() {
            Long reservationId = 1L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenThrow(new RuntimeException("Reservation not found"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> reservationGuestServiceImpl.cancelReservation(reservationId));

            assertEquals("Reservation not found", exception.getMessage());
        }

        @Test
        void cancelReservation_alreadyCancelled_shouldThrowException() {
            Long reservationId = 1L;

            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);
            testReservation.setBookingStatus(BookingStatus.CANCELLED);

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(testReservation);
            doThrow(new IllegalStateException("Cannot modify a cancelled reservation"))
                    .when(reservationServiceHelper).validateReservationCancellable(testReservation);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> reservationGuestServiceImpl.cancelReservation(reservationId));

            assertEquals("Cannot modify a cancelled reservation", exception.getMessage());
        }
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

    private ReservationResponseDetail expectedCancelledResponse() {
        return new ReservationResponseDetail(
                1L, "Test User", 4, "Test Hotel",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                BookingStatus.CANCELLED, false, LocalDateTime.now(), BigDecimal.valueOf(100)
        );
    }
}