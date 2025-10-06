package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String UTF8_ENCODING = "UTF-8";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailServiceHelper emailHelper;

    @Override
    public void sendWelcomeEmail(User user) {
        if (!emailHelper.canSendEmails(user)) return;

        try {
            Context context = emailHelper.createFullContext(null, user, null);
            sendEmail(user.getEmail(), "Welcome to SleepUp!", "welcome", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending welcome email to {}: {}", user.getEmail(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending welcome email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    public void sendOwnerReservedNotification(Reservation reservation) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User owner = reservation.getAccommodation().getManagedBy();
            User guest = reservation.getUser();
            Context context = emailHelper.createFullContext(reservation, owner, null);
            context.setVariable("guestName", guest.getName());

            sendEmail(owner.getEmail(), "Your property has just been booked!", "owner_reservation_notification", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending reservation notification to owner: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending reservation notification to owner: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendGuestReservationConfirmationEmail(Reservation reservation, BigDecimal discountAmount) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User guest = reservation.getUser();
            Context context = emailHelper.createFullContext(reservation, guest, discountAmount);
            sendEmail(guest.getEmail(), "Your Reservation Confirmation!", "guest_reservation_confirmation", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending confirmation email to guest: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending confirmation email to guest: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendGuestReservationReminderEmail(Reservation reservation) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User guest = reservation.getUser();
            Context context = emailHelper.createFullContext(reservation, guest, null);
            sendEmail(guest.getEmail(), "Upcoming Reservation Reminder", "guest_reminder", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending reservation reminder to guest: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending reservation reminder to guest: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendOwnerReservationReminderEmail(Reservation reservation) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User owner = reservation.getAccommodation().getManagedBy();
            Context context = emailHelper.createFullContext(reservation, owner, null);
            sendEmail(owner.getEmail(), "Upcoming Reservation at Your Property", "owner_reminder", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending owner reservation reminder: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending owner reservation reminder: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendCancellationConfirmationEmail(Reservation reservation) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User guest = reservation.getUser();
            Context context = emailHelper.createFullContext(reservation, guest, null);
            sendEmail(guest.getEmail(), "Your reservation has been successfully cancelled", "guest_cancellationByGuest", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending cancellation confirmation: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending cancellation confirmation: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendCancellationByOwnerNotificationEmail(Reservation reservation) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User guest = reservation.getUser();
            Context context = emailHelper.createFullContext(reservation, guest, null);
            sendEmail(guest.getEmail(), "Your reservation has been cancelled", "guest_cancellationByOwner", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending owner cancellation notification: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending owner cancellation notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendCancellationNotificationToOwnerEmail(Reservation reservation) {
        if (!emailHelper.canSendReservationEmails(reservation)) return;

        try {
            User owner = reservation.getAccommodation().getManagedBy();
            Context context = emailHelper.createFullContext(reservation, owner, null);
            sendEmail(owner.getEmail(), "Reservation at your property has been cancelled", "owner_cancellationByGuest", context);
        } catch (MessagingException e) {
            log.error("MessagingException while sending cancellation to owner: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending cancellation to owner: {}", e.getMessage(), e);
        }
    }

    private void sendEmail(String toEmail, String subject, String templateName, Context context) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        String htmlContent = templateEngine.process(templateName, context);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("{} email sent successfully to: {}", subject, toEmail);
    }
}
