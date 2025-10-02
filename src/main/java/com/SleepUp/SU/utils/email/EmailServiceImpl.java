package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailService;
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
    private static final String DASHBOARD_URL = "http://localhost:8080/swagger-ui/index.html#/";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendWelcomeEmail(User user) throws MessagingException {
        Context context = createFullContext(null, user, null);
        sendEmail(user.getEmail(), "Welcome to SleepUp!", "welcome", context);
    }

    @Override
    public void sendOwnerReservedNotification(Reservation reservation) throws MessagingException {
        User owner = getOwner(reservation);
        User guest = getGuest(reservation);
        Context context = createFullContext(reservation, owner, null);
        context.setVariable("guestName", guest.getName());

        sendEmail(owner.getEmail(), "Your property has just been booked!", "owner_reservation_notification", context);
    }

    @Override
    public void sendGuestReservationConfirmationEmail(Reservation reservation, BigDecimal discountAmount) throws MessagingException {
        User guest = getGuest(reservation);
        Context context = createFullContext(reservation, guest, discountAmount);

        sendEmail(guest.getEmail(), "Your reservation Confirmation!", "guest_reservation_confirmation", context);
    }

    @Override
    public void sendGuestReservationReminderEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        Context context = createFullContext(reservation, guest, null);

        sendEmail(guest.getEmail(), "Upcoming Reservation Reminder", "guest_reminder", context);
    }

    @Override
    public void sendOwnerReservationReminderEmail(Reservation reservation) throws MessagingException {
        User owner = getOwner(reservation);
        Context context = createFullContext(reservation, owner, null);

        sendEmail(owner.getEmail(), "Upcoming Reservation at Your Property", "owner_reminder", context);
    }

    @Override
    public void sendCancellationConfirmationEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        Context context = createFullContext(reservation, guest, null);

        sendEmail(guest.getEmail(), "Your reservation has been successfully cancelled", "guest_cancellationByGuest", context);
    }

    @Override
    public void sendCancellationByOwnerNotificationEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        Context context = createFullContext(reservation, guest, null);

        sendEmail(guest.getEmail(), "Your reservation has been cancelled", "guest_cancellationByOwner", context);
    }

    @Override
    public void sendCancellationNotificationToOwnerEmail(Reservation reservation) throws MessagingException {
        User owner = getOwner(reservation);
        Context context = createFullContext(reservation, owner, null);

        sendEmail(owner.getEmail(), "Reservation at your property has been cancelled", "owner_cancellationByGuest", context);
    }

    private Context createFullContext(Reservation reservation, User user, BigDecimal discountAmount) {
        Context context = new Context();

        context.setVariable("dashboardUrl", DASHBOARD_URL);

        if (reservation != null) {
            Accommodation accommodation = reservation.getAccommodation();
            context.setVariable("accommodationName", accommodation.getName());
            context.setVariable("location", accommodation.getLocation());
            context.setVariable("checkInDate", reservation.getCheckInDate() != null ? reservation.getCheckInDate().toString() : "N/A");
            context.setVariable("checkOutDate", reservation.getCheckOutDate() != null ? reservation.getCheckOutDate().toString() : "N/A");
            context.setVariable("amount", reservation.getTotalPrice());
        } else {
            context.setVariable("accommodationName", "Default Accommodation");
            context.setVariable("location", "Default Location");
            context.setVariable("checkInDate", "N/A");
            context.setVariable("checkOutDate", "N/A");
            context.setVariable("amount", "N/A");
        }

        if (user != null) {
            context.setVariable("userName", user.getName());
            context.setVariable("guestName", user.getName());
        } else {
            context.setVariable("userName", "Default User");
            context.setVariable("guestName", "Unknown Guest");
        }

        context.setVariable("discountAmount", discountAmount != null ? discountAmount : null);

        return context;
    }

    private User getOwner(Reservation reservation) {
        return reservation.getAccommodation().getManagedBy();
    }

    private User getGuest(Reservation reservation) {
        return reservation.getUser();
    }

    public void sendEmail(String toEmail, String subject, String templateName, Context context) throws MessagingException {
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
