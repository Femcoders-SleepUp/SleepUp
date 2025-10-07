package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.ReservationModificationException;
import com.SleepUp.SU.reservation.exceptions.ReservationNotFoundByIdException;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.utils.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationOwnerServiceImplTest {

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
    private ReservationOwnerServiceImpl reservationOwnerServiceImpl;

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
    class getAllReservationsOnMyAccommodationTest{
        @Test
        void getAllReservationsOnMyAccommodation_emptyList_shouldReturnEmptyList() {
            when(reservationRepository.findByAccommodationId(accommodationId))
                    .thenReturn(Collections.emptyList());

            List<ReservationResponseSummary> result = reservationOwnerServiceImpl.getReservationsForMyAccommodation(accommodationId);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(reservationRepository).findByAccommodationId(accommodationId);
        }

        @Test
        void getAllReservationsOnMyAccommodation_nonEmptyList_shouldReturnDTOs() {
            Reservation reservation1 = new Reservation();
            Reservation reservation2 = new Reservation();
            List<Reservation> entities = List.of(reservation1, reservation2);

            List<ReservationResponseSummary> responseList = List.of(response1, response2);

            when(reservationRepository.findByAccommodationId(accommodationId))
                    .thenReturn(entities);

            when(entityUtil.mapEntitiesToDTOs(
                    anyList(),
                    any(Function.class)
            )).thenReturn(responseList);

            List<ReservationResponseSummary> result =
                    reservationOwnerServiceImpl.getReservationsForMyAccommodation(accommodationId);

            assertThat(result).isSameAs(responseList);
        }
    }

    @Nested
    class UpdateStatusTest {

        @Test
        void updateStatus_nonExistingReservation_shouldThrow() {
            Long id = 99L;
            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CONFIRMED);

            when(reservationServiceHelper.getReservationEntityById(id)).thenThrow(new ReservationNotFoundByIdException(id));

            assertThatThrownBy(() ->
                    reservationOwnerServiceImpl.updateStatus(id, authRequest))
                    .isInstanceOf(ReservationNotFoundByIdException.class)
                    .hasMessage("Reservation with id '99' not found");

            verify(reservationMapper, never()).toDetail(any());
        }

        @Test
        void updateStatus_existingReservation_shouldUpdateBookingStatusAndReturnDetail() {
            Long id = 77L;
            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CANCELLED);

            Accommodation accommodation = new Accommodation();
            accommodation.setPrice(10.0);

            User user = new User();
            user.setId(1L);
            accommodation.setManagedBy(user);

            Reservation existing = new Reservation();
            existing.setBookingStatus(BookingStatus.PENDING);
            existing.setCheckInDate(LocalDate.now());
            existing.setCheckOutDate(LocalDate.now().plusDays(3));
            existing.setAccommodation(accommodation);
            existing.setTotalPrice(BigDecimal.valueOf(100.0));

            when(reservationServiceHelper.getReservationEntityById(id)).thenReturn(existing);
            ReservationResponseDetail detailDto = mock(ReservationResponseDetail.class);
            when(reservationMapper.toDetail(existing)).thenReturn(detailDto);

            ReservationResponseDetail result = reservationOwnerServiceImpl.updateStatus(id, authRequest);

            assertThat(existing.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(result).isSameAs(detailDto);
            verify(emailService).sendCancellationByOwnerNotificationEmail(existing);
            verify(reservationMapper).toDetail(existing);
        }

        @Test
        void updateStatus_statusNotChanged_shouldNotSendEmailButReturnDetail() {
            Long id = 100L;
            BookingStatus currentStatus = BookingStatus.CONFIRMED;

            ReservationAuthRequest authRequest = new ReservationAuthRequest(currentStatus);

            Reservation existing = new Reservation();
            existing.setBookingStatus(currentStatus);
            existing.setTotalPrice(BigDecimal.valueOf(150.0));

            when(reservationServiceHelper.getReservationEntityById(id)).thenReturn(existing);
            ReservationResponseDetail detailDto = mock(ReservationResponseDetail.class);
            when(reservationMapper.toDetail(existing)).thenReturn(detailDto);

            ReservationResponseDetail result = reservationOwnerServiceImpl.updateStatus(id, authRequest);

            assertThat(existing.getBookingStatus()).isEqualTo(currentStatus);
            assertThat(result).isSameAs(detailDto);

            verify(emailService, never()).sendGuestReservationConfirmationEmail(any(), any());
            verify(emailService, never()).sendCancellationByOwnerNotificationEmail(any());
            verify(reservationMapper).toDetail(existing);
        }

        @Test
        void updateStatus_currentStatusCancelled_shouldThrowException() {
            Long id = 101L;
            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CONFIRMED);

            Reservation existing = new Reservation();
            existing.setBookingStatus(BookingStatus.CANCELLED);

            when(reservationServiceHelper.getReservationEntityById(id)).thenReturn(existing);

            assertThatThrownBy(() -> reservationOwnerServiceImpl.updateStatus(id, authRequest))
                    .isInstanceOf(ReservationModificationException.class)
                    .hasMessage("Cannot modify a cancelled reservation");

            verify(emailService, never()).sendGuestReservationConfirmationEmail(any(), any());
            verify(emailService, never()).sendCancellationByOwnerNotificationEmail(any());
            verify(reservationMapper, never()).toDetail(any());
        }

        @Test
        void updateStatus_updateToConfirmed_shouldSendConfirmationEmail() {
            Long id = 102L;
            ReservationAuthRequest authRequest = new ReservationAuthRequest(BookingStatus.CONFIRMED);

            Reservation existing = new Reservation();
            existing.setBookingStatus(BookingStatus.PENDING);
            existing.setTotalPrice(BigDecimal.valueOf(200.0));

            when(reservationServiceHelper.getReservationEntityById(id)).thenReturn(existing);
            ReservationResponseDetail detailDto = mock(ReservationResponseDetail.class);
            when(reservationMapper.toDetail(existing)).thenReturn(detailDto);

            ReservationResponseDetail result = reservationOwnerServiceImpl.updateStatus(id, authRequest);

            assertThat(existing.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(result).isSameAs(detailDto);

            verify(emailService).sendGuestReservationConfirmationEmail(existing, existing.getTotalPrice());
            verify(emailService, never()).sendCancellationByOwnerNotificationEmail(any());
            verify(reservationMapper).toDetail(existing);
        }
    }
}
