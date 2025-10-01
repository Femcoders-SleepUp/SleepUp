package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationAccessEvaluatorTest {

    @Mock
    private AccommodationServiceHelper accommodationServiceHelper;

    @InjectMocks
    private AccommodationAccessEvaluator accommodationAccessEvaluator;

    @Test
    void testIsOwner_true() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId)).thenReturn(true);

        boolean result = accommodationAccessEvaluator.isOwner(accommodationId, userId);

        assertTrue(result);
        verify(accommodationServiceHelper, times(1)).isAccommodationOwnedByUser(accommodationId, userId);
    }

    @Test
    void testIsOwner_false() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(accommodationServiceHelper.isAccommodationOwnedByUser(accommodationId, userId)).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            accommodationAccessEvaluator.isOwner(accommodationId, userId);
        });

        assertEquals(
                "User ID 2 cannot access Accommodation ID 1. Only the owner is authorized to access this resource.",
                exception.getMessage()
        );

        verify(accommodationServiceHelper, times(1)).isAccommodationOwnedByUser(accommodationId, userId);
    }
}
