package com.SleepUp.SU.accommodation;

import com.SleepUp.SU.accommodation.dto.AccommodationMapper;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AccommodationMapperTest {

    private AccommodationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AccommodationMapper.class);
    }

    @Test
    void testToSummaryMapping() {
        Accommodation accommodation = Accommodation.builder()
                .id(1L)
                .name("Cozy Apartment")
                .price(120.0)
                .guestNumber(4)
                .petFriendly(true)
                .location("Paris")
                .description("Near Eiffel Tower")
                .imageUrl("img.png")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.of(2025, 1, 1))
                .availableTo(LocalDate.of(2025, 12, 31))
                .build();

        AccommodationResponseSummary summary = mapper.toSummary(accommodation);

        assertNotNull(summary);
        assertEquals("Cozy Apartment", summary.name());
        assertEquals(120.0, summary.price());
        assertEquals("Paris", summary.location());
    }

    @Test
    void testToEntityMapping() {
        User user = new User();
        user.setId(99L);
        user.setUsername("hostUser");

        AccommodationRequest request = new AccommodationRequest(
                "Beach House",
                250.0,
                6,
                true,
                "Miami",
                "Oceanfront property",
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 10, 1),
                null // MultipartFile will not be mapped directly here
        );

        Accommodation accommodation = mapper.toEntity(request, user);

        assertNotNull(accommodation);
        assertNull(accommodation.getId()); // because we ignored it in mapper
        assertEquals("Beach House", accommodation.getName());
        assertEquals(250.0, accommodation.getPrice());
        assertEquals(6, accommodation.getGuestNumber());
        assertTrue(accommodation.getPetFriendly());
        assertEquals("Miami", accommodation.getLocation());
        assertEquals("Oceanfront property", accommodation.getDescription());
        assertEquals(LocalTime.of(15, 0), accommodation.getCheckInTime());
        assertEquals(LocalDate.of(2025, 5, 1), accommodation.getAvailableFrom());
        assertEquals(user, accommodation.getManagedBy());
    }
}