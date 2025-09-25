package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationAccessEvaluatorTest {

    private ReservationRepository reservationRepository;
    private ReservationAccessEvaluator reservationAccessEvaluator;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        reservationAccessEvaluator = new ReservationAccessEvaluator(reservationRepository);
    }

    @Test
    void testIsReservationGuest_true() {
        Long reservationId = 1L;
        Long userId = 2L;

        when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(true);

        boolean result = reservationAccessEvaluator.isReservationGuest(reservationId, userId);
        assertTrue(result);

        verify(reservationRepository, times(1)).existsByIdAndUser_Id(reservationId, userId);
    }

    @Test
    void testIsReservationGuest_false() {
        Long reservationId = 1L;
        Long userId = 2L;

        when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            reservationAccessEvaluator.isReservationGuest(reservationId, userId);
        });

        String expectedMessage = "User ID 2 cannot access Reservation ID 1. Only reservation guests can access this information.";
        assertEquals(expectedMessage, exception.getMessage());

        verify(reservationRepository, times(1)).existsByIdAndUser_Id(reservationId, userId);
    }
}
