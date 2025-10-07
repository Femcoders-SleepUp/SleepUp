package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceHelperTest {

    private final EmailServiceHelper helper = new EmailServiceHelper();

    @Nested
    class CanSendEmailsTests {

        @Test
        void canSendEmails_withValidUser_shouldReturnTrue() {
            User user = new User();
            user.setEmail("test@example.com");
            assertTrue(helper.canSendEmails(user));
        }

        @Test
        void canSendEmails_withNullUser_shouldReturnFalse() {
            assertFalse(helper.canSendEmails(null));
        }
    }

    @Nested
    class CanSendReservationEmailsTests {

        @Test
        void canSendReservationEmails_withFullValidData_shouldReturnTrue() {
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
        void canSendReservationEmails_withNullReservation_shouldReturnFalse() {
            assertFalse(helper.canSendReservationEmails(null));
        }

        @Test
        void canSendReservationEmails_withNullUserOnReservation_shouldReturnFalse() {
            Reservation res = new Reservation();
            res.setUser(null);

            assertFalse(helper.canSendReservationEmails(res));
        }

        @Test
        void canSendReservationEmails_withNullAccommodation_shouldReturnFalse() {
            User guest = new User();
            guest.setEmail("guest@example.com");

            Reservation res = new Reservation();
            res.setUser(guest);
            res.setAccommodation(null);

            assertFalse(helper.canSendReservationEmails(res));
        }

        @Test
        void canSendReservationEmails_withNullManagedBy_shouldReturnFalse() {
            User guest = new User();
            guest.setEmail("guest@example.com");

            Accommodation acc = new Accommodation();
            acc.setManagedBy(null);

            Reservation res = new Reservation();
            res.setUser(guest);
            res.setAccommodation(acc);

            assertFalse(helper.canSendReservationEmails(res));
        }

        @Test
        void canSendReservationEmails_withOwnerEmailNullOrEmpty_shouldReturnFalse() {
            User guest = new User();
            guest.setEmail("guest@example.com");

            User owner = new User();
            owner.setEmail(null);

            Accommodation acc = new Accommodation();
            acc.setManagedBy(owner);

            Reservation res = new Reservation();
            res.setUser(guest);
            res.setAccommodation(acc);

            assertFalse(helper.canSendReservationEmails(res));

            owner.setEmail("");
            assertFalse(helper.canSendReservationEmails(res));
        }
    }

    @Nested
    class CreateFullContextTests {

        @Test
        void createFullContext_withReservationAndUser_shouldSetAllVariables() {
            User user = new User();
            user.setName("John Doe");
            user.setEmail("john@example.com");

            Accommodation accommodation = new Accommodation();
            accommodation.setName("Seaside Villa");
            accommodation.setLocation("Malibu");
            accommodation.setManagedBy(user);

            Reservation reservation = new Reservation();
            reservation.setId(1L);
            reservation.setAccommodation(accommodation);
            reservation.setUser(user);
            reservation.setCheckInDate(LocalDate.of(2025, 12, 20));
            reservation.setCheckOutDate(LocalDate.of(2025, 12, 25));
            reservation.setTotalPrice(BigDecimal.valueOf(1200));

            BigDecimal discount = BigDecimal.valueOf(100);

            Context context = helper.createFullContext(reservation, user, discount);

            assertThat(context.getVariable("dashboardUrl")).isNotNull();
            assertThat(context.getVariable("reservationUrl")).isNotNull();
            assertThat(context.getVariable("accommodationUrl")).isNotNull();
            assertThat(context.getVariable("accommodationName")).isEqualTo("Seaside Villa");
            assertThat(context.getVariable("location")).isEqualTo("Malibu");
            assertThat(context.getVariable("checkInDate")).isEqualTo("2025-12-20");
            assertThat(context.getVariable("checkOutDate")).isEqualTo("2025-12-25");
            assertThat(context.getVariable("amount")).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(context.getVariable("userName")).isEqualTo("John Doe");
            assertThat(context.getVariable("guestName")).isEqualTo("John Doe");
            assertThat(context.getVariable("discountAmount")).isEqualTo(discount);
        }

        @Test
        void createFullContext_withReservationNullUserNotNull_shouldSetUserVariablesAndDefaults() {
            User user = new User();
            user.setName("Jane Smith");
            user.setEmail("jane@example.com");

            BigDecimal discount = BigDecimal.valueOf(50);

            Context context = helper.createFullContext(null, user, discount);

            assertThat(context.getVariable("dashboardUrl")).isNotNull();
            assertThat(context.getVariable("reservationUrl")).isNull();
            assertThat(context.getVariable("accommodationUrl")).isNull();
            assertThat(context.getVariable("accommodationName")).isEqualTo("Default Accommodation");
            assertThat(context.getVariable("location")).isEqualTo("Default Location");
            assertThat(context.getVariable("checkInDate")).isEqualTo("N/A");
            assertThat(context.getVariable("checkOutDate")).isEqualTo("N/A");
            assertThat(context.getVariable("amount")).isEqualTo("N/A");
            assertThat(context.getVariable("userName")).isEqualTo("Jane Smith");
            assertThat(context.getVariable("guestName")).isEqualTo("Jane Smith");
            assertThat(context.getVariable("discountAmount")).isEqualTo(discount);
        }

        @Test
        void createFullContext_withReservationAndUserNull_shouldSetAllDefaults() {
            BigDecimal discount = BigDecimal.ZERO;

            Context context = helper.createFullContext(null, null, discount);

            assertThat(context.getVariable("dashboardUrl")).isNotNull();
            assertThat(context.getVariable("reservationUrl")).isNull();
            assertThat(context.getVariable("accommodationUrl")).isNull();
            assertThat(context.getVariable("accommodationName")).isEqualTo("Default Accommodation");
            assertThat(context.getVariable("location")).isEqualTo("Default Location");
            assertThat(context.getVariable("checkInDate")).isEqualTo("N/A");
            assertThat(context.getVariable("checkOutDate")).isEqualTo("N/A");
            assertThat(context.getVariable("amount")).isEqualTo("N/A");
            assertThat(context.getVariable("userName")).isEqualTo("Default User");
            assertThat(context.getVariable("guestName")).isEqualTo("Unknown Guest");
            assertThat(context.getVariable("discountAmount")).isEqualTo(discount);
        }

        @Test
        void createFullContext_withReservationCheckInOrOutDatesNull_shouldUseNAForDates() {
            User user = new User();
            user.setName("Guest");

            Accommodation accommodation = new Accommodation();
            accommodation.setName("Mountain Lodge");
            accommodation.setLocation("Aspen");
            accommodation.setManagedBy(user);

            Reservation reservation = new Reservation();
            reservation.setAccommodation(accommodation);
            reservation.setUser(user);
            reservation.setCheckInDate(null);
            reservation.setCheckOutDate(null);
            reservation.setTotalPrice(BigDecimal.valueOf(300));

            BigDecimal discount = BigDecimal.valueOf(20);

            Context context = helper.createFullContext(reservation, user, discount);

            assertThat(context.getVariable("checkInDate")).isEqualTo("N/A");
            assertThat(context.getVariable("checkOutDate")).isEqualTo("N/A");
            assertThat(context.getVariable("amount")).isEqualTo(BigDecimal.valueOf(300));
        }
    }

    @Nested
    class SetReservationUrlTests {
        @Test
        void setReservationUrl_withValidReservation_shouldReturnCorrectUrl() {
            Reservation reservation = new Reservation();
            reservation.setId(100L);
            String url = helper.setReservationUrl(reservation);
            assertTrue(url.contains("/api/v1/reservations/100"));
        }
    }

    @Nested
    class SetAccommodationUrlTests {
        @Test
        void setAccommodationUrl_withValidAccommodation_shouldReturnCorrectUrl() {
            Accommodation accommodation = new Accommodation();
            accommodation.setId(200L);
            String url = helper.setAccommodationUrl(accommodation);
            assertTrue(url.contains("/api/v1/accommodations/200"));
        }
    }

    @Nested
    class GetDashboardUrlTests {
        @Test
        void getDashboardUrl_shouldReturnCorrectUrl() {
            assertEquals("http://localhost:8080/swagger-ui/index.html#", helper.getDashboardUrl());
        }
    }
}