package com.SleepUp.SU.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.config.properties.MailProperties;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceHelperTest {

    @Mock
    private EmailService emailService;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private EmailServiceHelper emailServiceHelper;

    private User guest;
    private User owner;
    private Accommodation accommodation;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        guest = new User();
        guest.setEmail("guest@test.com");
        guest.setName("John Doe");

        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setName("Jane Smith");

        accommodation = new Accommodation();
        accommodation.setName("Cozy Apartment");
        accommodation.setLocation("Madrid, Spain");
        accommodation.setManagedBy(owner);

        reservation = new Reservation();
        reservation.setUser(guest);
        reservation.setAccommodation(accommodation);
        reservation.setCheckInDate(LocalDate.of(2025, 10, 1));
        reservation.setCheckOutDate(LocalDate.of(2025, 10, 5));
        reservation.setTotalPrice(BigDecimal.valueOf(100));
    }

    @Nested
    class ValidateEmailConfigGroup {
        @Test
        void validateEmailConfig_validConfig_shouldEnableEmail() {
            when(mailProperties.getFrom()).thenReturn("from@test.com");
            when(mailProperties.getUsername()).thenReturn("username");
            when(mailProperties.getPassword()).thenReturn("password");

            emailServiceHelper.mailProperties = mailProperties;

            emailServiceHelper.validateEmailConfig();
        }

        @Test
        void validateEmailConfig_missingFields_shouldDisableEmail() {
            when(mailProperties.getFrom()).thenReturn(null);

            emailServiceHelper.mailProperties = mailProperties;

            emailServiceHelper.validateEmailConfig();
        }
    }

    @Nested
    class SendWelcomeEmailGroup {
        @Test
        void sendWelcomeEmail_serviceCallSuccess_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendWelcomeEmail(any(User.class));

            emailServiceHelper.sendWelcomeEmail(guest);

            verify(emailService).sendWelcomeEmail(guest);
        }

        @Test
        void sendWelcomeEmail_serviceThrowsException_shouldHandleGracefullyAndNotThrow() throws MessagingException {
            doThrow(new MessagingException("Error")).when(emailService).sendWelcomeEmail(any(User.class));

            assertDoesNotThrow(() -> emailServiceHelper.sendWelcomeEmail(guest));

            verify(emailService).sendWelcomeEmail(guest);
        }
    }


    @Nested
    class SendOwnerReservedNotificationGroup {
        @Test
        void sendOwnerReservedNotification_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendOwnerReservedNotification(any(Reservation.class));

            emailServiceHelper.sendOwnerReservedNotification(reservation);

            verify(emailService).sendOwnerReservedNotification(reservation);
        }
    }

    @Nested
    class SendReservationConfirmationEmailGroup {
        @Test
        void sendReservationConfirmationEmail_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendGuestReservationConfirmationEmail(any(Reservation.class), any(BigDecimal.class));

            emailServiceHelper.sendReservationConfirmationEmail(reservation, BigDecimal.TEN);

            verify(emailService).sendGuestReservationConfirmationEmail(reservation, BigDecimal.TEN);
        }
    }

    @Nested
    class SendReservationReminderEmailGroup {
        @Test
        void sendReservationReminderEmail_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendGuestReservationReminderEmail(any(Reservation.class));

            emailServiceHelper.sendReservationReminderEmail(reservation);

            verify(emailService).sendGuestReservationReminderEmail(reservation);
        }
    }

    @Nested
    class SendOwnerReservationReminderEmailGroup {
        @Test
        void sendOwnerReservationReminderEmail_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendOwnerReservationReminderEmail(any(Reservation.class));

            emailServiceHelper.sendOwnerReservationReminderEmail(reservation);

            verify(emailService).sendOwnerReservationReminderEmail(reservation);
        }
    }

    @Nested
    class SendCancellationConfirmationEmailGroup {
        @Test
        void sendCancellationConfirmationEmail_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendCancellationConfirmationEmail(any(Reservation.class));

            emailServiceHelper.sendCancellationConfirmationEmail(reservation);

            verify(emailService).sendCancellationConfirmationEmail(reservation);
        }
    }

    @Nested
    class SendCancellationByOwnerNotificationEmailGroup {
        @Test
        void sendCancellationByOwnerNotificationEmail_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendCancellationByOwnerNotificationEmail(any(Reservation.class));

            emailServiceHelper.sendCancellationByOwnerNotificationEmail(reservation);

            verify(emailService).sendCancellationByOwnerNotificationEmail(reservation);
        }
    }

    @Nested
    class SendCancellationNotificationToOwnerEmailGroup {
        @Test
        void sendCancellationNotificationToOwnerEmail_Success_shouldCallEmailService() throws MessagingException {
            doNothing().when(emailService).sendCancellationNotificationToOwnerEmail(any(Reservation.class));

            emailServiceHelper.sendCancellationNotificationToOwnerEmail(reservation);

            verify(emailService).sendCancellationNotificationToOwnerEmail(reservation);
        }
    }

    @Nested
    class HandleNewReservationEmailsGroup {
        @Test
        void handleNewReservationEmails_Success_shouldSendBothEmails() throws MessagingException {
            doNothing().when(emailService).sendGuestReservationConfirmationEmail(any(Reservation.class), any(BigDecimal.class));
            doNothing().when(emailService).sendOwnerReservedNotification(any(Reservation.class));

            emailServiceHelper.handleNewReservationEmails(reservation, BigDecimal.valueOf(3));

            verify(emailService).sendGuestReservationConfirmationEmail(reservation, BigDecimal.valueOf(3));
            verify(emailService).sendOwnerReservedNotification(reservation);
        }
    }

    @Nested
    class HandleGuestCancellationEmailsGroup {
        @Test
        void handleGuestCancellationEmails_Success_shouldSendBothEmails() throws MessagingException {
            doNothing().when(emailService).sendCancellationConfirmationEmail(any(Reservation.class));
            doNothing().when(emailService).sendCancellationNotificationToOwnerEmail(any(Reservation.class));

            emailServiceHelper.handleGuestCancellationEmails(reservation);

            verify(emailService).sendCancellationConfirmationEmail(reservation);
            verify(emailService).sendCancellationNotificationToOwnerEmail(reservation);
        }
    }

    @Nested
    class HandleOwnerCancellationEmailsGroup {
        @Test
        void handleOwnerCancellationEmails_Success_shouldSendNotification() throws MessagingException {
            doNothing().when(emailService).sendCancellationByOwnerNotificationEmail(any(Reservation.class));

            emailServiceHelper.handleOwnerCancellationEmails(reservation);

            verify(emailService).sendCancellationByOwnerNotificationEmail(reservation);
        }
    }

    @Nested
    class SendReservationRemindersGroup {
        @Test
        void sendReservationReminders_Success_shouldSendBothReminders() throws MessagingException {
            doNothing().when(emailService).sendGuestReservationReminderEmail(any(Reservation.class));
            doNothing().when(emailService).sendOwnerReservationReminderEmail(any(Reservation.class));

            emailServiceHelper.sendReservationReminders(reservation);

            verify(emailService).sendGuestReservationReminderEmail(reservation);
            verify(emailService).sendOwnerReservationReminderEmail(reservation);
        }
    }

    @Nested
    class CanSendEmailsGroup {
        @Test
        void canSendEmails_WithValidUser_ReturnsTrue() {
            boolean result = emailServiceHelper.canSendEmails(guest);

            assertTrue(result);
        }

        @Test
        void canSendEmails_WithNullUser_ReturnsFalse() {
            assertFalse(emailServiceHelper.canSendEmails(null));
        }

        @Test
        void canSendEmails_WithNullEmail_ReturnsFalse() {
            guest.setEmail(null);
            assertFalse(emailServiceHelper.canSendEmails(guest));
        }

        @Test
        void canSendEmails_WithEmptyEmail_ReturnsFalse() {
            guest.setEmail("   ");
            assertFalse(emailServiceHelper.canSendEmails(guest));
        }
    }

    @Nested
    class CanSendReservationEmailsGroup {
        @Test
        void canSendReservationEmails_WithValidData_ReturnsTrue() {
            assertTrue(emailServiceHelper.canSendReservationEmails(reservation));
        }

        @Test
        void canSendReservationEmails_WithInvalidGuest_ReturnsFalse() {
            guest.setEmail(null);
            assertFalse(emailServiceHelper.canSendReservationEmails(reservation));
        }

        @Test
        void canSendReservationEmails_WithNullAccommodation_ReturnsFalse() {
            reservation.setAccommodation(null);
            assertFalse(emailServiceHelper.canSendReservationEmails(reservation));
        }

        @Test
        void canSendReservationEmails_WithNullOwner_ReturnsFalse() {
            accommodation.setManagedBy(null);
            assertFalse(emailServiceHelper.canSendReservationEmails(reservation));
        }

        @Test
        void canSendReservationEmails_WithNullReservation_ReturnsFalse() {
            assertFalse(emailServiceHelper.canSendReservationEmails(null));
        }

        @Test
        void canSendReservationEmails_WithInvalidOwnerEmail_ReturnsFalse() {
            owner.setEmail("");
            assertFalse(emailServiceHelper.canSendReservationEmails(reservation));
        }
    }

    @Nested
    class ExceptionHandlingGroup {
        @Test
        void allEmailMethods_HandleExceptionsGracefully() throws MessagingException {
            doThrow(new MessagingException("Error")).when(emailService)
                    .sendGuestReservationConfirmationEmail(any(Reservation.class), any(BigDecimal.class));
            doThrow(new MessagingException("Error")).when(emailService)
                    .sendOwnerReservedNotification(any(Reservation.class));

            assertDoesNotThrow(() ->
                    emailServiceHelper.handleNewReservationEmails(reservation, BigDecimal.valueOf(3))
            );
        }
    }
}
