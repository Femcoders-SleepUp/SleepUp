package com.SleepUp.SU.reservation.security;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationAccessEvaluatorTest {

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private AccommodationServiceHelper accommodationServiceHelper;

    @InjectMocks
    private ReservationAccessEvaluator reservationAccessEvaluator;

    private final Reservation reservationMock = mock(Reservation.class);
    private  final Accommodation accommodationMock = mock(Accommodation.class);

    private final Long reservationId = 1L;
    private final Long userId = 2L;
    private final Long accommodationId = 100L;

    @Nested
    class IsReservationGuest {

        @Test
        void isReservationGuest_reservationExistsForUser_shouldReturnTrue() {

            when(reservationServiceHelper.isReservationGuestTheUser(reservationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationGuest(reservationId, userId);
            assertTrue(result);

            verify(reservationServiceHelper, times(1)).isReservationGuestTheUser(reservationId, userId);
        }

        @Test
        void isReservationGuest_reservationDoesNotExistForUser_shouldReturnFalse() {

            when(reservationServiceHelper.isReservationGuestTheUser(reservationId, userId)).thenReturn(false);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                reservationAccessEvaluator.isReservationGuest(reservationId, userId);
            });

            String expectedMessage = " Cause: This reservation was not created by you.";
            assertEquals(expectedMessage, exception.getMessage());

            verify(reservationServiceHelper, times(1)).isReservationGuestTheUser(reservationId, userId);
        }
    }

    @Nested
    class IsReservationGuestOrOwner {

        @Test
        void isReservationGuestOrOwner_guestIsTrue_shouldReturnTrue() {
            
            when(reservationServiceHelper.getAccommodationIdFromReservationId(reservationId)).thenReturn(accommodationId);
            when(reservationServiceHelper.isReservationGuestTheUser(reservationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationGuestOrOwner(reservationId, userId);
            assertTrue(result);

            verify(reservationServiceHelper).isReservationGuestTheUser(reservationId, userId);
        }

        @Test
        void isReservationGuestOrOwner_ownerIsTrue_shouldReturnTrue() {

            when(reservationServiceHelper.getAccommodationIdFromReservationId(reservationId)).thenReturn(accommodationId);
            when(reservationServiceHelper.isReservationGuestTheUser(reservationId, userId)).thenReturn(false);
            when(accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationGuestOrOwner(reservationId, userId);
            assertTrue(result);

            verify(reservationServiceHelper).isReservationGuestTheUser(reservationId, userId);
            verify(accommodationServiceHelper).isAccommodationOwnedByUser(accommodationId, userId);
        }

        @Test
        void isReservationGuestOrOwner_neitherGuestNorOwner_shouldReturnFalse() {
            
            when(reservationServiceHelper.getAccommodationIdFromReservationId(reservationId)).thenReturn(accommodationId);
            when(reservationServiceHelper.isReservationGuestTheUser(reservationId, userId)).thenReturn(false);
            when(accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId)).thenReturn(false);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                reservationAccessEvaluator.isReservationGuestOrOwner(reservationId, userId);
            });

            String expectedMessage = " Cause: This reservation was not created by you or does not belong to any of your accommodations.";
            assertEquals(expectedMessage, exception.getMessage());

            verify(reservationServiceHelper).isReservationGuestTheUser(reservationId, userId);
            verify(accommodationServiceHelper).isAccommodationOwnedByUser(accommodationId, userId);
        }
    }

    @Nested
    class IsReservationAccommodationOwner {

        @Test
        void isReservationAccommodationOwner_ownerIsTrue_shouldReturnTrue() {
            when(reservationServiceHelper.getAccommodationIdFromReservationId(reservationId)).thenReturn(accommodationId);
            when(accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId)).thenReturn(true);

            boolean result = reservationAccessEvaluator.isReservationAccommodationOwner(reservationId, userId);
            assertTrue(result);

            verify(accommodationServiceHelper).isAccommodationOwnedByUser(accommodationId, userId);
            verify(reservationServiceHelper).getAccommodationIdFromReservationId(reservationId);
        }

        @Test
        void isReservationAccommodationOwner_ownerIsFalse_shouldReturnFalse() {

            when(reservationServiceHelper.getAccommodationIdFromReservationId(reservationId)).thenReturn(accommodationId);
            when(accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId)).thenReturn(false);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
                reservationAccessEvaluator.isReservationAccommodationOwner(reservationId, userId);
            });

            String expectedMessage = " Cause: This reservation does not belong to any of your accommodations.";
            assertEquals(expectedMessage, exception.getMessage());

            verify(accommodationServiceHelper).isAccommodationOwnedByUser(accommodationId, userId);
            verify(reservationServiceHelper).getAccommodationIdFromReservationId(reservationId);
        }
    }
}