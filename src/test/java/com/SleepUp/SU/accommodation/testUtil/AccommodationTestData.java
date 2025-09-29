package com.SleepUp.SU.accommodation.testUtil;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.user.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.mock.web.MockMultipartFile;

public class AccommodationTestData {

    public static final User TEST_USER = new User();

    public static AccommodationRequest defaultAccommodationRequestBuilder() {
        return AccommodationRequest.builder()
                .name("Test Accommodation")
                .price(100.0)
                .petFriendly(true)
                .guestNumber(2)
                .location("Test Location")
                .description("Test Description")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.now())
                .availableTo(LocalDate.now().plusDays(10))
                .image(new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[0]))
                .build();
    }

    public static AccommodationRequest defaultUpdateRequestBuilder() {
        return AccommodationRequest.builder()
                .name("Updated Accommodation")
                .price(120.0)
                .petFriendly(false)
                .guestNumber(3)
                .location("Updated Location")
                .description("Updated Description")
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(12, 0))
                .availableFrom(LocalDate.now())
                .availableTo(LocalDate.now().plusDays(20))
                .image(new MockMultipartFile("image", "update.jpg", "image/jpeg", new byte[0]))
                .build();
    }

    public static Accommodation defaultAccommodation() {
        return Accommodation.builder()
                .id(1L)
                .name("Test Accommodation")
                .price(100.0)
                .petFriendly(true)
                .guestNumber(2)
                .location("Test Location")
                .description("Test Description")
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .availableFrom(LocalDate.now())
                .availableTo(LocalDate.now().plusDays(10))
                .build();
    }

    public static AccommodationResponseDetail defaultAccommodationResponseDetailBuilder() {
        return AccommodationResponseDetail.builder()
                .id(1L)
                .name("Test Accommodation")
                .price(100.0)
                .petFriendly(true)
                .guestNumber(2)
                .location("Test Location")
                .description("Test Description")
                .checkInTime(LocalTime.of(14, 0).toString())
                .checkOutTime(LocalTime.of(11, 0).toString())
                .availableFrom(LocalDate.now().toString())
                .availableTo(LocalDate.now().plusDays(10).toString())
                .managedByUsername("testUser")
                .imageUrl("http://image.url")
                .build();
    }

    public static AccommodationResponseSummary defaultAccommodationResponseSummaryBuilder() {
        return AccommodationResponseSummary.builder()
                .id(1L)
                .name("Test Accommodation")
                .price(100.0)
                .guestNumber(2)
                .petFriendly(true)
                .location("Test Location")
                .imageUrl("http://image.url")
                .build();
    }
}
