package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.security.AccommodationAccessEvaluator;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationAccessEvaluatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private AccommodationAccessEvaluator accommodationAccessEvaluator;

    @InjectMocks
    private ReservationAccessEvaluator reservationAccessEvaluator;

    private final Reservation reservationMock = mock(Reservation.class);
    private  final Accommodation accommodationMock = mock(Accommodation.class);

    @Nested
    class IsReservationGuest {

        @Test
        void isReservationGuest_reservationExistsForUser_shouldReturnTrue() {
            Long reservationId = 1L;
            Long userId = 2L;

            when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationGuest(reservationId, userId);
            assertTrue(result);

            verify(reservationRepository, times(1)).existsByIdAndUser_Id(reservationId, userId);
        }

        @Test
        void isReservationGuest_reservationDoesNotExistForUser_shouldReturnFalse() {
            Long reservationId = 1L;
            Long userId = 2L;

            when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(false);

            boolean result = reservationAccessEvaluator.isReservationGuest(reservationId, userId);
            assertFalse(result);

//            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
//                reservationAccessEvaluator.isReservationGuest(reservationId, userId);
//            });
//
//            String expectedMessage = "User ID 2 cannot access Reservation ID 1. Only reservation guests can access this information.";
//            assertEquals(expectedMessage, exception.getMessage());

            verify(reservationRepository, times(1)).existsByIdAndUser_Id(reservationId, userId);
        }
    }

    @Nested
    class IsReservationGuestOrOwner {

        @Test
        void isReservationGuestOrOwner_guestIsTrue_shouldReturnTrue() {
            Long reservationId = 1L;
            Long userId = 2L;
            Long accommodationId = 100L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(reservationMock);
            when(reservationMock.getAccommodation()).thenReturn(accommodationMock);
            when(accommodationMock.getId()).thenReturn(accommodationId);

            when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationGuestOrOwner(reservationId, userId);
            assertTrue(result);

            verify(reservationRepository).existsByIdAndUser_Id(reservationId, userId);
        }

        @Test
        void isReservationGuestOrOwner_ownerIsTrue_shouldReturnTrue() {
            Long reservationId = 1L;
            Long userId = 2L;
            Long accommodationId = 100L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(reservationMock);
            when(reservationMock.getAccommodation()).thenReturn(accommodationMock);
            when(accommodationMock.getId()).thenReturn(accommodationId);

            when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(false);
            when(accommodationAccessEvaluator.isOwner(accommodationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationGuestOrOwner(reservationId, userId);
            assertTrue(result);

            verify(reservationRepository).existsByIdAndUser_Id(reservationId, userId);
            verify(accommodationAccessEvaluator).isOwner(accommodationId, userId);
        }

        @Test
        void isReservationGuestOrOwner_neitherGuestNorOwner_shouldReturnFalse() {
            Long reservationId = 1L;
            Long userId = 2L;
            Long accommodationId = 100L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(reservationMock);
            when(reservationMock.getAccommodation()).thenReturn(accommodationMock);
            when(accommodationMock.getId()).thenReturn(accommodationId);

            when(reservationRepository.existsByIdAndUser_Id(reservationId, userId)).thenReturn(false);
            when(accommodationAccessEvaluator.isOwner(accommodationId, userId)).thenReturn(false);

            boolean result = reservationAccessEvaluator.isReservationGuestOrOwner(reservationId, userId);
            assertFalse(result);

            verify(reservationRepository).existsByIdAndUser_Id(reservationId, userId);
            verify(accommodationAccessEvaluator).isOwner(accommodationId, userId);
        }
    }

    @Nested
    class IsReservationAccommodationOwner {

        @Test
        void isReservationAccommodationOwner_ownerIsTrue_shouldReturnTrue() {
            Long reservationId = 1L;
            Long userId = 2L;
            Long accommodationId = 100L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(reservationMock);
            when(reservationMock.getAccommodation()).thenReturn(accommodationMock);
            when(accommodationMock.getId()).thenReturn(accommodationId);

            when(accommodationAccessEvaluator.isOwner(accommodationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationAccommodationOwner(reservationId, userId);
            assertTrue(result);

            verify(accommodationAccessEvaluator).isOwner(accommodationId, userId);
        }

        @Test
        void isReservationAccommodationOwner_ownerIsFalse_shouldReturnFalse() {
            Long reservationId = 1L;
            Long userId = 2L;
            Long accommodationId = 100L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(reservationMock);
            when(reservationMock.getAccommodation()).thenReturn(accommodationMock);
            when(accommodationMock.getId()).thenReturn(accommodationId);

            when(accommodationAccessEvaluator.isOwner(accommodationId, userId)).thenReturn(false);

            boolean result = reservationAccessEvaluator.isReservationAccommodationOwner(reservationId, userId);
            assertFalse(result);

            verify(accommodationAccessEvaluator).isOwner(accommodationId, userId);
        }
    }
}
