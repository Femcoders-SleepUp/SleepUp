package com.SleepUp.SU.reservation.accommodationOwner;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.dto.ReservationAuthRequest;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.entity.Reservation;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

//    @Nested
//    class getAllReservationsOnMyAccommodationTest{
//        @Test
//        void getAllReservationsOnMyAccommodation_emptyList_shouldThrow() {
//            when(reservationRepository.findByAccommodationId(accommodationId))
//                    .thenReturn(Collections.emptyList());
//
//            assertThatThrownBy(() ->
//                    reservationOwnerServiceImpl.getAllReservationsOnMyAccommodation(dummyUser, accommodationId))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessage("Empty list");
//
//            verify(reservationRepository).findByAccommodationId(accommodationId);
//            verify(entityUtil, never()).mapEntitiesToDTOs(anyList(), any());
//        }
//
//        @Test
//        void getAllReservationsOnMyAccommodation_nonEmptyList_shouldReturnDTOs() {
//            Reservation reservation1 = new Reservation();
//            Reservation reservation2 = new Reservation();
//            List<Reservation> entities = List.of(reservation1, reservation2);
//
//            List<ReservationResponseSummary> responseList = List.of(response1, response2);
//
//            when(reservationRepository.findByAccommodationId(accommodationId))
//                    .thenReturn(entities);
//
//            when(entityUtil.mapEntitiesToDTOs(
//                    anyList(),
//                    any(Function.class)
//            )).thenReturn(responseList);
//
//            List<ReservationResponseSummary> result =
//                    reservationOwnerServiceImpl.getAllReservationsOnMyAccommodation(dummyUser, accommodationId);
//
//            assertThat(result).isSameAs(responseList);
//        }
//    }

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

}
