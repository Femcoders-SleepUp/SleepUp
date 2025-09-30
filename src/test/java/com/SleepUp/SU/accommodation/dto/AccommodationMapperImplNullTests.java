package com.SleepUp.SU.accommodation.dto;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccommodationMapperImplNullTests {

    private AccommodationMapperImpl mapper;

    @BeforeEach
    void setup() {
        mapper = new AccommodationMapperImpl();
    }

    @Test
    void toSummary_givenNullAccommodation_shouldReturnNull() {
        AccommodationResponseSummary result = mapper.toSummary(null);
        assertNull(result, "Expected null when passing null Accommodation");
    }

    @Test
    void toSummary_withNullPetFriendly_shouldNotThrowAndMapOtherFields() {
        Accommodation accommodation = Accommodation.builder()
                .id(1L)
                .name("Test Name")
                .price(100.0)
                .guestNumber(2)
                .petFriendly(null)
                .location("Test Location")
                .imageUrl("test-url")
                .build();

        AccommodationResponseSummary summary = mapper.toSummary(accommodation);

        assertNotNull(summary);
        assertEquals(1L, summary.id());
        assertEquals("Test Name", summary.name());
        assertEquals(100.0, summary.price());
        assertEquals(2, summary.guestNumber());
        assertEquals(Boolean.FALSE, summary.petFriendly(), "petFriendly should default to false instead of null");
        assertEquals("Test Location", summary.location());
        assertEquals("test-url", summary.imageUrl());
    }

    @Test
    void toEntity_givenNullAccommodationRequestAndUser_shouldReturnNull() {
        Accommodation accommodation = mapper.toEntity(null, null);
        assertNull(accommodation, "Expected null when both accommodationRequest and user are null");
    }

    @Test
    void toEntity_givenNullAccommodationRequestWithUser_shouldMapUserAndDefaultFields() {
        User user = new User();
        Accommodation accommodation = mapper.toEntity(null, user);

        assertNotNull(accommodation);
        assertEquals(user, accommodation.getManagedBy(), "User should be set in accommodation");
        // other fields should be default (null/zero)
        assertNull(accommodation.getName());
        assertNull(accommodation.getLocation());
    }

    @Test
    void toDetail_givenNullAccommodation_shouldReturnNull() {
        AccommodationResponseDetail detail = mapper.toDetail(null);
        assertNull(detail, "Expected null when passing null Accommodation");
    }

    @Test
    void toDetail_withNullPetFriendly_shouldNotThrowAndMapOtherFields() {
        User user = new User();
        user.setName("Test User");

        Accommodation accommodation = Accommodation.builder()
                .id(1L)
                .name("Test Name")
                .price(100.0)
                .guestNumber(2)
                .petFriendly(null)
                .location("Test Location")
                .description("Test description")
                .checkInTime(null)
                .checkOutTime(null)
                .availableFrom(null)
                .availableTo(null)
                .managedBy(user)
                .imageUrl("test-url")
                .build();

        AccommodationResponseDetail detail = mapper.toDetail(accommodation);

        assertNotNull(detail);
        assertEquals("Test User", detail.managedByUsername());
        assertEquals(Boolean.FALSE, detail.petFriendly(), "petFriendly should default to false instead of null");
        assertEquals("Test Name", detail.name());
        assertEquals(100.0, detail.price());
        assertEquals(2, detail.guestNumber());
        assertEquals("test-url", detail.imageUrl());
    }

    @Test
    void accommodationManagedByName_whenAccommodationOrUserOrNameIsNull_shouldReturnNull() throws Exception {
        assertNull(invokePrivateManagedByName(null), "Expected null for null accommodation");

        Accommodation accommodation = Accommodation.builder().managedBy(null).build();
        assertNull(invokePrivateManagedByName(accommodation), "Expected null if managedBy is null");

        User userWithNullName = new User();
        userWithNullName.setName(null);
        accommodation = Accommodation.builder().managedBy(userWithNullName).build();
        assertNull(invokePrivateManagedByName(accommodation), "Expected null if managedBy name is null");

        userWithNullName.setName("Valid Name");
        accommodation = Accommodation.builder().managedBy(userWithNullName).build();
        assertEquals("Valid Name", invokePrivateManagedByName(accommodation));
    }

    private String invokePrivateManagedByName(Accommodation accommodation) throws Exception {
        var method = AccommodationMapperImpl.class.getDeclaredMethod("accommodationManagedByName", Accommodation.class);
        method.setAccessible(true);
        return (String) method.invoke(mapper, accommodation);
    }
}
