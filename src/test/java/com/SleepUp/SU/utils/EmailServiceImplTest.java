package com.SleepUp.SU.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

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
        reservation.setCheckInDate(LocalDate.of(2025, 10, 1));
        reservation.setCheckOutDate(LocalDate.of(2025, 10, 5));
        reservation.setUser(guest);
        reservation.setAccommodation(accommodation);
    }

    @Test
    void sendWelcomeEmail_validUser_shouldUseWelcomeTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome"), any(Context.class)))
                .thenReturn("<html>Welcome Email</html>");

        emailService.sendWelcomeEmail(guest);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("welcome"), any(Context.class));
    }

    @Test
    void sendOwnerReservedNotification_validReservation_shouldUseNotificationTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("owner_reservation_notification"), any(Context.class)))
                .thenReturn("<html>Owner Notification</html>");

        emailService.sendOwnerReservedNotification(reservation);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("owner_reservation_notification"), any(Context.class));
    }

    @Test
    void sendGuestReservationConfirmationEmail_validReservation_shouldUseConfirmationTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("guest_reservation_confirmation"), any(Context.class)))
                .thenReturn("<html>Confirmation</html>");

        emailService.sendGuestReservationConfirmationEmail(reservation, BigDecimal.valueOf(3));

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("guest_reservation_confirmation"), any(Context.class));
    }

    @Test
    void sendGuestReservationReminderEmail_validReservation_shouldUseReminderTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("guest_reminder"), any(Context.class)))
                .thenReturn("<html>Reminder</html>");

        emailService.sendGuestReservationReminderEmail(reservation);

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("guest_reminder"), any(Context.class));
    }

    @Test
    void sendOwnerReservationReminderEmail_validReservation_shouldUseOwnerReminderTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("owner_reminder"), any(Context.class)))
                .thenReturn("<html>Owner Reminder</html>");

        emailService.sendOwnerReservationReminderEmail(reservation);

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("owner_reminder"), any(Context.class));
    }

    @Test
    void sendCancellationConfirmationEmail_validReservation_shouldUseGuestCancellationTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("guest_cancellationByGuest"), any(Context.class)))
                .thenReturn("<html>Cancellation</html>");

        emailService.sendCancellationConfirmationEmail(reservation);

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("guest_cancellationByGuest"), any(Context.class));
    }

    @Test
    void sendCancellationByOwnerNotificationEmail_validReservation_shouldUseOwnerCancellationTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("guest_cancellationByOwner"), any(Context.class)))
                .thenReturn("<html>Cancellation by Owner</html>");

        emailService.sendCancellationByOwnerNotificationEmail(reservation);

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("guest_cancellationByOwner"), any(Context.class));
    }

    @Test
    void sendCancellationNotificationToOwnerEmail_validReservation_shouldUseOwnerNotificationTemplateAndSend() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("owner_cancellationByGuest"), any(Context.class)))
                .thenReturn("<html>Cancellation to Owner</html>");

        emailService.sendCancellationNotificationToOwnerEmail(reservation);

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("owner_cancellationByGuest"), any(Context.class));
    }

    @Test
    void sendEmailMethods_defaultBehavior_shouldCreateMimeMessageOnce() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Email</html>");

        emailService.sendWelcomeEmail(guest);

        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendEmailMethods_multipleCalls_shouldVerifyInteractions() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Email</html>");

        emailService.sendWelcomeEmail(guest);
        emailService.sendGuestReservationConfirmationEmail(reservation, BigDecimal.valueOf(3));
        emailService.sendCancellationConfirmationEmail(reservation);

        verify(mailSender, times(3)).send(mimeMessage);
    }
}
