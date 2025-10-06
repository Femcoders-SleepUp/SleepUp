package com.SleepUp.SU.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailServiceImplTestEmail {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private MimeMessage mimeMessage;

    private User owner;
    private User guest;
    private Accommodation accommodation;
    private Reservation reservation;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html>Test Email Content</html>");

        owner = new User();
        owner.setName("Owner User");
        owner.setEmail("sleepup.backend@gmail.com");

        guest = new User();
        guest.setName("Guest User");
        guest.setEmail("sleepup.backend@gmail.com");

        accommodation = new Accommodation();
        accommodation.setName("Seaside Villa");
        accommodation.setLocation("Beach City");
        accommodation.setManagedBy(owner);

        reservation = new Reservation();
        reservation.setUser(guest);
        reservation.setAccommodation(accommodation);
        reservation.setCheckInDate(LocalDate.now().plusDays(10));
        reservation.setCheckOutDate(LocalDate.now().plusDays(15));
        reservation.setTotalPrice(new BigDecimal("1200.00"));
    }

    @Test
    public void sendWelcomeEmail_validUser_shouldSendMimeMessage() throws MessagingException {
        emailService.sendWelcomeEmail(guest);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendOwnerReservedNotification_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendOwnerReservedNotification(reservation);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendGuestReservationConfirmationEmail_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendGuestReservationConfirmationEmail(reservation, new BigDecimal("100.00"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendGuestReservationReminderEmail_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendGuestReservationReminderEmail(reservation);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendOwnerReservationReminderEmail_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendOwnerReservationReminderEmail(reservation);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendCancellationConfirmationEmail_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendCancellationConfirmationEmail(reservation);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendCancellationByOwnerNotificationEmail_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendCancellationByOwnerNotificationEmail(reservation);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendCancellationNotificationToOwnerEmail_validReservation_shouldSendMimeMessage() throws MessagingException {
        emailService.sendCancellationNotificationToOwnerEmail(reservation);
        verify(mailSender).send(any(MimeMessage.class));
    }
}
