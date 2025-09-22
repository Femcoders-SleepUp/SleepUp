package com.SleepUp.SU.reservation;

import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.utils.EntityUtil;
import com.SleepUp.SU.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
class ReservationOwnerServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private EntityUtil entityUtil;

    @InjectMocks
    private ReservationOwnerService reservationOwnerService;

    private final User dummyUser = new User();
    private final Long accommodationId = 55L;

    @Test
    void getAllReservationsOnMyAccommodation_emptyList_shouldThrow() {
        when(reservationRepository.findByAccommodationId(accommodationId))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() ->
                reservationOwnerService.getAllReservationsOnMyAccommodation(dummyUser, accommodationId))
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

        ReservationResponseSummary response1 = new ReservationResponseSummary(
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
        ReservationResponseSummary response2 = new ReservationResponseSummary(
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
        List<ReservationResponseSummary> responseList = List.of(response1, response2);

        when(reservationRepository.findByAccommodationId(accommodationId))
                .thenReturn(entities);

        when(entityUtil.mapEntitiesToDTOs(
                anyList(),
                any(Function.class)
        )).thenReturn(responseList);

        List<ReservationResponseSummary> result =
                reservationOwnerService.getAllReservationsOnMyAccommodation(dummyUser, accommodationId);

        assertThat(result).isSameAs(responseList);
    }
}
