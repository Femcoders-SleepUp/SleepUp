package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.user.security.UserAccessEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccommodationAccessEvaluatorTest {

    private AccommodationRepository accommodationRepository;
    private UserAccessEvaluator userAccessEvaluator;
    private AccommodationAccessEvaluator accommodationAccessEvaluator;

    @BeforeEach
    void setUp() {
        accommodationRepository = Mockito.mock(AccommodationRepository.class);
        userAccessEvaluator = Mockito.mock(UserAccessEvaluator.class);
        accommodationAccessEvaluator = new AccommodationAccessEvaluator(accommodationRepository, userAccessEvaluator);
    }

    @Test
    void testIsOwner_true() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId)).thenReturn(true);

        boolean result = accommodationAccessEvaluator.isOwner(accommodationId, userId);

        assertTrue(result);
        verify(accommodationRepository, times(1)).existsByIdAndManagedBy_Id(accommodationId, userId);
        verifyNoInteractions(userAccessEvaluator);
    }

    @Test
    void testIsOwner_false() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId)).thenReturn(false);

        boolean result = accommodationAccessEvaluator.isOwner(accommodationId, userId);

        assertFalse(result);
        verify(accommodationRepository, times(1)).existsByIdAndManagedBy_Id(accommodationId, userId);
        verifyNoInteractions(userAccessEvaluator);
    }

    @Test
    void testIsOwnerOrAdmin_whenAdmin() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(userAccessEvaluator.isAdmin(userId)).thenReturn(true);

        boolean result = accommodationAccessEvaluator.isOwnerOrAdmin(userId, accommodationId);

        assertTrue(result);
        verify(userAccessEvaluator, times(1)).isAdmin(userId);
        verifyNoMoreInteractions(accommodationRepository);
    }

    @Test
    void testIsOwnerOrAdmin_whenOwner() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(userAccessEvaluator.isAdmin(userId)).thenReturn(false);
        when(accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId)).thenReturn(true);

        boolean result = accommodationAccessEvaluator.isOwnerOrAdmin(userId, accommodationId);

        assertTrue(result);
        verify(userAccessEvaluator, times(1)).isAdmin(userId);
        verify(accommodationRepository, times(1)).existsByIdAndManagedBy_Id(accommodationId, userId);
    }

    @Test
    void testIsOwnerOrAdmin_whenNeither() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(userAccessEvaluator.isAdmin(userId)).thenReturn(false);
        when(accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId)).thenReturn(false);

        boolean result = accommodationAccessEvaluator.isOwnerOrAdmin(userId, accommodationId);

        assertFalse(result);
        verify(userAccessEvaluator, times(1)).isAdmin(userId);
        verify(accommodationRepository, times(1)).existsByIdAndManagedBy_Id(accommodationId, userId);
    }
}
