package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceHelperTest {

    private final EmailServiceHelper helper = new EmailServiceHelper();

    @Test
    void testCanSendEmails_WithValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        assertTrue(helper.canSendEmails(user));
    }

    @Test
    void testCanSendEmails_WithNullUser() {
        assertFalse(helper.canSendEmails(null));
    }

    @Test
    void testCanSendReservationEmails_FullValidData() {
        User guest = new User();
        guest.setEmail("guest@example.com");

        User owner = new User();
        owner.setEmail("owner@example.com");

        Accommodation acc = new Accommodation();
        acc.setManagedBy(owner);
        acc.setName("Hotel Test");
        acc.setLocation("Paris");
        acc.setId(1L);

        Reservation res = new Reservation();
        res.setUser(guest);
        res.setAccommodation(acc);
        res.setId(10L);

        assertTrue(helper.canSendReservationEmails(res));
    }

    @Test
    void testCanSendReservationEmails_NullReservation() {
        assertFalse(helper.canSendReservationEmails(null));
    }

    @Test
    void testCreateFullContext_WithReservationAndUser() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("test@example.com");

        Accommodation acc = new Accommodation();
        acc.setName("Beach Resort");
        acc.setLocation("Miami");
        acc.setId(2L);
        acc.setManagedBy(user);

        Reservation res = new Reservation();
        res.setId(5L);
        res.setAccommodation(acc);
        res.setUser(user);
        res.setCheckInDate(LocalDate.of(2025, 10, 10));
        res.setCheckOutDate(LocalDate.of(2025, 10, 15));
        res.setTotalPrice(BigDecimal.valueOf(500));

        BigDecimal discount = BigDecimal.valueOf(50);

        Context context = helper.createFullContext(res, user, discount);

        assertEquals("Beach Resort", context.getVariable("accommodationName"));
        assertEquals("Miami", context.getVariable("location"));
        assertEquals("John Doe", context.getVariable("userName"));
        assertEquals(discount, context.getVariable("discountAmount"));
    }

    @Test
    void testSetReservationUrl() {
        Reservation res = new Reservation();
        res.setId(100L);
        String url = helper.setReservationUrl(res);
        assertTrue(url.contains("/api/v1/reservations/100"));
    }

    @Test
    void testSetAccommodationUrl() {
        Accommodation acc = new Accommodation();
        acc.setId(200L);
        String url = helper.setAccommodationUrl(acc);
        assertTrue(url.contains("/api/v1/accommodations/200"));
    }

    @Test
    void testGetDashboardUrl() {
        assertEquals("http://localhost:8080/swagger-ui/index.html#", helper.getDashboardUrl());
    }
}
