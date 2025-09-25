package com.SleepUp.SU.accommodation.security;

import com.SleepUp.SU.accommodation.AccommodationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccommodationAccessEvaluatorTest {

    private AccommodationRepository accommodationRepository;
    private AccommodationAccessEvaluator accommodationAccessEvaluator;

    @BeforeEach
    void setUp() {
        accommodationRepository = Mockito.mock(AccommodationRepository.class);
        accommodationAccessEvaluator = new AccommodationAccessEvaluator(accommodationRepository);
    }

    @Test
    void testIsOwner_true() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId)).thenReturn(true);

        boolean result = accommodationAccessEvaluator.isOwner(accommodationId, userId);

        assertTrue(result);
        verify(accommodationRepository, times(1)).existsByIdAndManagedBy_Id(accommodationId, userId);
    }

    @Test
    void testIsOwner_false() {
        Long accommodationId = 1L;
        Long userId = 2L;

        when(accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId)).thenReturn(false);

        boolean result = accommodationAccessEvaluator.isOwner(accommodationId, userId);

        assertFalse(result);
        verify(accommodationRepository, times(1)).existsByIdAndManagedBy_Id(accommodationId, userId);
    }
}
