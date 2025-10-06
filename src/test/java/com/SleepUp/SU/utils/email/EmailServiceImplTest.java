package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private EmailServiceHelper emailHelper;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendWelcomeEmail_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        when(emailHelper.canSendEmails(user)).thenReturn(true);
        when(emailHelper.createFullContext(null, user, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendWelcomeEmail(user);

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendWelcomeEmail_FailureOnCanSend() {
        User user = new User();
        user.setEmail("fail@example.com");
        when(emailHelper.canSendEmails(user)).thenReturn(false);
        emailService.sendWelcomeEmail(user);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendOwnerReservedNotification_Success() throws Exception {
        User owner = new User();
        owner.setEmail("owner@example.com");
        User guest = new User();
        guest.setName("Guest");
        Reservation reservation = new Reservation();
        reservation.setUser(guest);
        Accommodation acc = new Accommodation();
        acc.setManagedBy(owner);
        reservation.setAccommodation(acc);

        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, owner, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendOwnerReservedNotification(reservation);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendGuestReservationConfirmationEmail_Success() throws Exception {
        User guest = new User();
        guest.setEmail("guest@example.com");
        Reservation reservation = new Reservation();
        reservation.setUser(guest);
        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, guest, BigDecimal.valueOf(10))).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendGuestReservationConfirmationEmail(reservation, BigDecimal.valueOf(10));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendGuestReservationReminderEmail_Success() throws Exception {
        User guest = new User();
        guest.setEmail("guest@example.com");
        Reservation reservation = new Reservation();
        reservation.setUser(guest);
        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, guest, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendGuestReservationReminderEmail(reservation);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendOwnerReservationReminderEmail_Success() throws Exception {
        User owner = new User();
        owner.setEmail("owner@example.com");
        User guest = new User();
        guest.setName("Guest");
        Reservation reservation = new Reservation();
        reservation.setUser(guest);
        Accommodation acc = new Accommodation();
        acc.setManagedBy(owner);
        reservation.setAccommodation(acc);
        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, owner, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendOwnerReservationReminderEmail(reservation);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendCancellationConfirmationEmail_Success() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, user, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendCancellationConfirmationEmail(reservation);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendCancellationByOwnerNotificationEmail_Success() throws Exception {
        User guest = new User();
        guest.setEmail("guest@example.com");
        Reservation reservation = new Reservation();
        reservation.setUser(guest);
        User owner = new User();
        owner.setEmail("owner@example.com");
        Accommodation acc = new Accommodation();
        acc.setManagedBy(owner);
        reservation.setAccommodation(acc);
        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, guest, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendCancellationByOwnerNotificationEmail(reservation);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendCancellationNotificationToOwnerEmail_Success() throws Exception {
        User owner = new User();
        owner.setEmail("owner@example.com");
        Reservation reservation = new Reservation();
        Accommodation acc = new Accommodation();
        acc.setManagedBy(owner);
        reservation.setAccommodation(acc);

        when(emailHelper.canSendReservationEmails(reservation)).thenReturn(true);
        when(emailHelper.createFullContext(reservation, owner, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        emailService.sendCancellationNotificationToOwnerEmail(reservation);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendWelcomeEmail_LogsMailSendException() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        when(emailHelper.canSendEmails(user)).thenReturn(true);
        when(emailHelper.createFullContext(null, user, null)).thenReturn(new Context());
        when(templateEngine.process(anyString(), any())).thenReturn("<html></html>");

        doThrow(new MailSendException("simulated failure")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(user));

        verify(mailSender).send(any(MimeMessage.class));
    }


}
