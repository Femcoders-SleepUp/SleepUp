package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.config.properties.MailProperties;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.dto.UserRequest;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceHelperTest {

    @Mock
    private EmailService emailService;

    @Mock
    private Logger logger;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private EmailServiceHelper emailServiceHelper;

    private User user;
    private User owner;
    private Accommodation accommodation;
    private Reservation reservation;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setEmail("guest@test.com");

        owner = new User();
        owner.setEmail("owner@test.com");

        accommodation = new Accommodation();
        accommodation.setName("Beach House");
        accommodation.setManagedBy(owner);

        reservation = new Reservation();

        userRequest = UserRequest.builder()
                .username("GuestUser")
                .name("Name")
                .email("guest@test.com")
                .password("password123")
                .build();

        emailServiceHelper = spy(new EmailServiceHelper(emailService));
        emailServiceHelper.logger = logger;
    }

    @Nested
    class validateEmailConfigTests {
        @Test
        void validateEmailConfig_validConfig_shouldEnableEmail() {
            when(mailProperties.getFrom()).thenReturn("from@test.com");
            when(mailProperties.getUsername()).thenReturn("username");
            when(mailProperties.getPassword()).thenReturn("password");

            emailServiceHelper.mailProperties = mailProperties;

            emailServiceHelper.validateEmailConfig();

            verify(logger).info("Email configuration validated successfully. Email sending enabled.");
        }

        @Test
        void validateEmailConfig_missingFields_shouldDisableEmail() {
            when(mailProperties.getFrom()).thenReturn(null);

            emailServiceHelper.mailProperties = mailProperties;

            emailServiceHelper.validateEmailConfig();

            verify(logger).warn("Email sending is disabled due to missing or invalid mail configuration.");
        }
    }

    @Nested
    class sendWelcomeEmailTests {
        @Test
        void sendWelcomeEmail_validData_shouldSend() throws MessagingException {
            doNothing().when(emailService).sendWelcomeEmail(userRequest.email(), userRequest.username());

            emailServiceHelper.sendWelcomeEmail(userRequest, user);

            verify(emailService).sendWelcomeEmail(userRequest.email(), userRequest.username());
            verify(logger).info("Welcome email sent successfully to: {}", user.getEmail());
        }

        @Test
        void sendWelcomeEmail_serviceThrows_shouldLogWarning() throws MessagingException {
            doThrow(new MessagingException("error")).when(emailService).sendWelcomeEmail(anyString(), anyString());

            emailServiceHelper.sendWelcomeEmail(userRequest, user);

            verify(logger).warn("Failed to send welcome email to {}: {}", user.getEmail(), "error");
        }
    }

    @Nested
    class sendOwnerReservedNotificationTests {
        @Test
        void sendOwnerReservedNotification_validData_shouldSend() throws MessagingException {
            doNothing().when(emailService).sendOwnerReservedNotification(user, accommodation, reservation);

            emailServiceHelper.sendOwnerReservedNotification(user, accommodation, reservation);

            verify(emailService).sendOwnerReservedNotification(user, accommodation, reservation);
            verify(logger).info("New reservation sent successfully to: {}", user.getEmail());
        }

        @Test
        void sendOwnerReservedNotification_serviceThrows_shouldLogWarning() throws MessagingException {
            doThrow(new MessagingException("error")).when(emailService).sendOwnerReservedNotification(any(), any(), any());

            emailServiceHelper.sendOwnerReservedNotification(user, accommodation, reservation);

            verify(logger).warn("Failed to send new reservation email to {}: {}", user.getEmail(), "error");
        }
    }

    @Nested
    class sendReservationConfirmationEmailTests {
        @Test
        void sendReservationConfirmationEmail_validData_shouldSend() throws MessagingException {
            doNothing().when(emailService).sendReservationConfirmationEmail(user, accommodation, reservation);

            emailServiceHelper.sendReservationConfirmationEmail(user, accommodation, reservation);

            verify(emailService).sendReservationConfirmationEmail(user, accommodation, reservation);
            verify(logger).info("Reservation confirmation email sent successfully to: {}", user.getEmail());
        }

        @Test
        void sendReservationConfirmationEmail_serviceThrows_shouldLogWarning() throws MessagingException {
            doThrow(new MessagingException("error")).when(emailService).sendReservationConfirmationEmail(any(), any(), any());

            emailServiceHelper.sendReservationConfirmationEmail(user, accommodation, reservation);

            verify(logger).warn("Failed to send reservation confirmation email to {}: {}", user.getEmail(), "error");
        }
    }

    @Nested
    class canSendEmailsTests {
        @Test
        void canSendEmails_nullUser_shouldReturnFalse() {
            assertFalse(emailServiceHelper.canSendEmails(null));
        }

        @Test
        void canSendEmails_nullEmail_shouldReturnFalse() {
            user.setEmail(null);
            assertFalse(emailServiceHelper.canSendEmails(user));
        }

        @Test
        void canSendEmails_emptyEmail_shouldReturnFalse() {
            user.setEmail("   ");
            assertFalse(emailServiceHelper.canSendEmails(user));
        }

        @Test
        void canSendEmails_validEmail_shouldReturnTrue() {
            assertTrue(emailServiceHelper.canSendEmails(user));
        }
    }

    @Nested
    class canSendReservationEmailsTests {
        @Test
        void canSendReservationEmails_nullGuest_shouldReturnFalse() {
            assertFalse(emailServiceHelper.canSendReservationEmails(null, accommodation, reservation));
        }

        @Test
        void canSendReservationEmails_nullAccommodation_shouldReturnFalse() {
            assertFalse(emailServiceHelper.canSendReservationEmails(user, null, reservation));
        }

        @Test
        void canSendReservationEmails_nullOwner_shouldReturnFalse() {
            accommodation.setManagedBy(null);
            assertFalse(emailServiceHelper.canSendReservationEmails(user, accommodation, reservation));
        }

        @Test
        void canSendReservationEmails_invalidOwnerEmail_shouldReturnFalse() {
            owner.setEmail(null);
            assertFalse(emailServiceHelper.canSendReservationEmails(user, accommodation, reservation));
        }

        @Test
        void canSendReservationEmails_nullReservation_shouldReturnFalse() {
            assertFalse(emailServiceHelper.canSendReservationEmails(user, accommodation, null));
        }

        @Test
        void canSendReservationEmails_validData_shouldReturnTrue() {
            assertTrue(emailServiceHelper.canSendReservationEmails(user, accommodation, reservation));
        }
    }

    @Nested
    class flowHandlerTests {
        @Test
        void handleNewReservationEmails_validData_shouldCallSubMethods() throws MessagingException {
            doNothing().when(emailService).sendReservationConfirmationEmail(user, accommodation, reservation);
            doNothing().when(emailService).sendOwnerReservedNotification(user, accommodation, reservation);

            emailServiceHelper.handleNewReservationEmails(user, accommodation, reservation);

            verify(emailService).sendReservationConfirmationEmail(user, accommodation, reservation);
            verify(emailService).sendOwnerReservedNotification(user, accommodation, reservation);
        }

        @Test
        void handleGuestCancellationEmails_validData_shouldCallSubMethods() throws MessagingException {
            doNothing().when(emailService).sendCancellationConfirmationEmail(user, accommodation, reservation);
            doNothing().when(emailService).sendCancellationNotificationToOwnerEmail(user, accommodation, reservation);

            emailServiceHelper.handleGuestCancellationEmails(user, accommodation, reservation);

            verify(emailService).sendCancellationConfirmationEmail(user, accommodation, reservation);
            verify(emailService).sendCancellationNotificationToOwnerEmail(user, accommodation, reservation);
        }

        @Test
        void handleOwnerCancellationEmails_validData_shouldCallSubMethod() throws MessagingException {
            doNothing().when(emailService).sendCancellationByOwnerNotificationEmail(user, accommodation, reservation);

            emailServiceHelper.handleOwnerCancellationEmails(user, accommodation, reservation);

            verify(emailService).sendCancellationByOwnerNotificationEmail(user, accommodation, reservation);
        }

        @Test
        void sendReservationReminders_validData_shouldCallSubMethods() throws MessagingException {
            doNothing().when(emailService).sendReservationReminderEmail(user, accommodation, reservation);
            doNothing().when(emailService).sendOwnerReservationReminderEmail(accommodation, reservation);

            emailServiceHelper.sendReservationReminders(user, accommodation, reservation);

            verify(emailService).sendReservationReminderEmail(user, accommodation, reservation);
            verify(emailService).sendOwnerReservationReminderEmail(accommodation, reservation);
        }
    }
}
