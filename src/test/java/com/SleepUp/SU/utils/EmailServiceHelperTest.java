package com.SleepUp.SU.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.config.properties.MailProperties;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.dto.UserRequest;
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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private UserRequest userRequest;

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
        reservation.setCheckInDate(LocalDate.of(2025, 10, 1));
        reservation.setCheckOutDate(LocalDate.of(2025, 10, 5));

        userRequest = new UserRequest("test@test.com", "testuser", "password123", "Test User");
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
        void sendWelcomeEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

            emailServiceHelper.sendWelcomeEmail(userRequest, guest);

            verify(emailService).sendWelcomeEmail(userRequest.email(), userRequest.username());
        }

        @Test
        void sendWelcomeEmail_HandlesException() throws MessagingException {
            doThrow(new MessagingException("Error")).when(emailService)
                    .sendWelcomeEmail(anyString(), anyString());

            assertDoesNotThrow(() ->
                    emailServiceHelper.sendWelcomeEmail(userRequest, guest)
            );

            verify(emailService).sendWelcomeEmail(anyString(), anyString());
        }
    }

    @Nested
    class SendOwnerReservedNotificationGroup {
        @Test
        void sendOwnerReservedNotification_Success() throws MessagingException {
            doNothing().when(emailService).sendOwnerReservedNotification(any(), any(), any());

            emailServiceHelper.sendOwnerReservedNotification(guest, accommodation, reservation);

            verify(emailService).sendOwnerReservedNotification(guest, accommodation, reservation);
        }
    }

    @Nested
    class SendReservationConfirmationEmailGroup {
        @Test
        void sendReservationConfirmationEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendReservationConfirmationEmail(any(), any(), any());

            emailServiceHelper.sendReservationConfirmationEmail(guest, accommodation, reservation);

            verify(emailService).sendReservationConfirmationEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class SendReservationReminderEmailGroup {
        @Test
        void sendReservationReminderEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendReservationReminderEmail(any(), any(), any());

            emailServiceHelper.sendReservationReminderEmail(guest, accommodation, reservation);

            verify(emailService).sendReservationReminderEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class SendOwnerReservationReminderEmailGroup {
        @Test
        void sendOwnerReservationReminderEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendOwnerReservationReminderEmail(any(), any());

            emailServiceHelper.sendOwnerReservationReminderEmail(accommodation, reservation);

            verify(emailService).sendOwnerReservationReminderEmail(accommodation, reservation);
        }
    }

    @Nested
    class SendCancellationConfirmationEmailGroup {
        @Test
        void sendCancellationConfirmationEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendCancellationConfirmationEmail(any(), any(), any());

            emailServiceHelper.sendCancellationConfirmationEmail(guest, accommodation, reservation);

            verify(emailService).sendCancellationConfirmationEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class SendCancellationByOwnerNotificationEmailGroup {
        @Test
        void sendCancellationByOwnerNotificationEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendCancellationByOwnerNotificationEmail(any(), any(), any());

            emailServiceHelper.sendCancellationByOwnerNotificationEmail(guest, accommodation, reservation);

            verify(emailService).sendCancellationByOwnerNotificationEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class SendCancellationNotificationToOwnerEmailGroup {
        @Test
        void sendCancellationNotificationToOwnerEmail_Success() throws MessagingException {
            doNothing().when(emailService).sendCancellationNotificationToOwnerEmail(any(), any(), any());

            emailServiceHelper.sendCancellationNotificationToOwnerEmail(guest, accommodation, reservation);

            verify(emailService).sendCancellationNotificationToOwnerEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class HandleNewReservationEmailsGroup {
        @Test
        void handleNewReservationEmails_SendsBothEmails() throws MessagingException {
            doNothing().when(emailService).sendReservationConfirmationEmail(any(), any(), any());
            doNothing().when(emailService).sendOwnerReservedNotification(any(), any(), any());

            emailServiceHelper.handleNewReservationEmails(guest, accommodation, reservation);

            verify(emailService).sendReservationConfirmationEmail(guest, accommodation, reservation);
            verify(emailService).sendOwnerReservedNotification(guest, accommodation, reservation);
        }
    }

    @Nested
    class HandleGuestCancellationEmailsGroup {
        @Test
        void handleGuestCancellationEmails_SendsBothEmails() throws MessagingException {
            doNothing().when(emailService).sendCancellationConfirmationEmail(any(), any(), any());
            doNothing().when(emailService).sendCancellationNotificationToOwnerEmail(any(), any(), any());

            emailServiceHelper.handleGuestCancellationEmails(guest, accommodation, reservation);

            verify(emailService).sendCancellationConfirmationEmail(guest, accommodation, reservation);
            verify(emailService).sendCancellationNotificationToOwnerEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class HandleOwnerCancellationEmailsGroup {
        @Test
        void handleOwnerCancellationEmails_SendsNotification() throws MessagingException {
            doNothing().when(emailService).sendCancellationByOwnerNotificationEmail(any(), any(), any());

            emailServiceHelper.handleOwnerCancellationEmails(guest, accommodation, reservation);

            verify(emailService).sendCancellationByOwnerNotificationEmail(guest, accommodation, reservation);
        }
    }

    @Nested
    class SendReservationRemindersGroup {
        @Test
        void sendReservationReminders_SendsBothReminders() throws MessagingException {
            doNothing().when(emailService).sendReservationReminderEmail(any(), any(), any());
            doNothing().when(emailService).sendOwnerReservationReminderEmail(any(), any());

            emailServiceHelper.sendReservationReminders(guest, accommodation, reservation);

            verify(emailService).sendReservationReminderEmail(guest, accommodation, reservation);
            verify(emailService).sendOwnerReservationReminderEmail(accommodation, reservation);
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
            boolean result = emailServiceHelper.canSendEmails(null);

            assertFalse(result);
        }

        @Test
        void canSendEmails_WithNullEmail_ReturnsFalse() {
            guest.setEmail(null);

            boolean result = emailServiceHelper.canSendEmails(guest);

            assertFalse(result);
        }

        @Test
        void canSendEmails_WithEmptyEmail_ReturnsFalse() {
            guest.setEmail("   ");

            boolean result = emailServiceHelper.canSendEmails(guest);

            assertFalse(result);
        }
    }

    @Nested
    class CanSendReservationEmailsGroup {
        @Test
        void canSendReservationEmails_WithValidData_ReturnsTrue() {
            boolean result = emailServiceHelper.canSendReservationEmails(guest, accommodation, reservation);

            assertTrue(result);
        }

        @Test
        void canSendReservationEmails_WithInvalidGuest_ReturnsFalse() {
            guest.setEmail(null);

            boolean result = emailServiceHelper.canSendReservationEmails(guest, accommodation, reservation);

            assertFalse(result);
        }

        @Test
        void canSendReservationEmails_WithNullAccommodation_ReturnsFalse() {
            boolean result = emailServiceHelper.canSendReservationEmails(guest, null, reservation);

            assertFalse(result);
        }

        @Test
        void canSendReservationEmails_WithNullOwner_ReturnsFalse() {
            accommodation.setManagedBy(null);

            boolean result = emailServiceHelper.canSendReservationEmails(guest, accommodation, reservation);

            assertFalse(result);
        }

        @Test
        void canSendReservationEmails_WithNullReservation_ReturnsFalse() {
            boolean result = emailServiceHelper.canSendReservationEmails(guest, accommodation, null);

            assertFalse(result);
        }

        @Test
        void canSendReservationEmails_WithInvalidOwnerEmail_ReturnsFalse() {
            owner.setEmail("");

            boolean result = emailServiceHelper.canSendReservationEmails(guest, accommodation, reservation);

            assertFalse(result);
        }
    }

    @Nested
    class ExceptionHandlingGroup {
        @Test
        void allEmailMethods_HandleExceptionsGracefully() throws MessagingException {
            doThrow(new MessagingException("Error")).when(emailService)
                    .sendReservationConfirmationEmail(any(), any(), any());
            doThrow(new MessagingException("Error")).when(emailService)
                    .sendOwnerReservedNotification(any(), any(), any());

            assertDoesNotThrow(() ->
                    emailServiceHelper.handleNewReservationEmails(guest, accommodation, reservation)
            );
        }
    }
}
