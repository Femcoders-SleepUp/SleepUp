package com.SleepUp.SU.reservation.owner;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.exceptions.ReservationNotFoundByIdException;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReservationOwnerServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private EmailServiceHelper emailServiceHelper;

    @Mock
    private EntityUtil entityUtil;

    @InjectMocks
    private ReservationOwnerServiceImpl reservationOwnerServiceImpl;

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
                true,
                LocalDateTime.of(2025, 9, 1, 10, 30)
        );
        response2 = new ReservationResponseSummary(
                2L,
                "bob",
                4,
                "Mountain Cabin",
                LocalDate.of(2025, 10, 5),
                LocalDate.of(2025, 10, 12),
                BookingStatus.PENDING,
                false,
                LocalDateTime.of(2025, 9, 15, 14, 0)
        );

    }

    @Nested
    class getAllReservationsOnMyAccommodationTest{
        @Test
        void getAllReservationsOnMyAccommodation_emptyList_shouldThrow() {
            when(reservationRepository.findByAccommodationId(accommodationId))
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                    reservationOwnerServiceImpl.getAllReservationsOnMyAccommodation(dummyUser, accommodationId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Empty list");

            verify(reservationRepository).findByAccommodationId(accommodationId);
            verify(entityUtil, never()).mapEntitiesToDTOs(anyList(), any());
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
                    reservationOwnerServiceImpl.getAllReservationsOnMyAccommodation(dummyUser, accommodationId);

            assertThat(result).isSameAs(responseList);
        }
    }

    @Nested
    class updateStatusTest{
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

            Reservation existing = new Reservation();
            existing.setBookingStatus(BookingStatus.PENDING);

            when(reservationServiceHelper.getReservationEntityById(id)).thenReturn(existing);
            ReservationResponseDetail detailDto = mock(ReservationResponseDetail.class);
            when(reservationMapper.toDetail(existing)).thenReturn(detailDto);

            ReservationResponseDetail result =
                    reservationOwnerServiceImpl.updateStatus(id, authRequest);

            assertThat(existing.getBookingStatus())
                    .isEqualTo(BookingStatus.CANCELLED);
            assertThat(result).isSameAs(detailDto);
            verify(reservationMapper).toDetail(existing);
        }
    }

    @Nested
    class getReservationByIdTest {

        @Test
        void getReservationById_nonExisting_shouldThrow() {
            Long id = 123L;

            when(reservationServiceHelper.getReservationEntityById(id)).thenThrow(new ReservationNotFoundByIdException(id));

            assertThatThrownBy(() ->
                    reservationOwnerServiceImpl.getReservationById(id))
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
                    reservationOwnerServiceImpl.getReservationById(id);

            assertThat(result).isSameAs(detailDto);
            verify(reservationMapper).toDetail(isExisting);
        }
    }

}
