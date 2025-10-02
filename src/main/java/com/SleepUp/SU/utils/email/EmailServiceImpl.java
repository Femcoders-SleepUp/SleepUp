package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
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
    private static final String DASHBOARD_URL = "http://localhost:8080/swagger-ui/index.html#/";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendWelcomeEmail(User user) throws MessagingException {
        Context context = createContext();
        context.setVariable("userName", user.getName());

        sendEmail(user.getEmail(), "Welcome to SleepUp!", "WelcomeUser", context);
    }

    @Override
    public void sendOwnerReservedNotification(Reservation reservation) throws MessagingException {
        User owner = getOwner(reservation);
        User guest = getGuest(reservation);
        Context context = createReservationContext(reservation);
        context.setVariable("ownerName", owner.getName());
        context.setVariable("guestName", guest.getName());

        sendEmail(owner.getEmail(), "Your property has just been booked!", "NotificationReservationOwner", context);
    }

    @Override
    public void sendGuestReservationConfirmationEmail(Reservation reservation, BigDecimal discountAmount) throws MessagingException {
        User guest = getGuest(reservation);
        Context context = createReservationContext(reservation);
        context.setVariable("userName", guest.getName());
        context.setVariable("discountAmount", discountAmount);

        sendEmail(guest.getEmail(), "Your Booking Confirmation!", "ConfirmationGuest", context);
    }

    @Override
    public void sendReservationReminderEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        Context context = createReservationContext(reservation);
        context.setVariable("guestName", guest.getName());

        sendEmail(guest.getEmail(), "Upcoming Reservation Reminder", "ReminderGuestReservation", context);
    }

    @Override
    public void sendOwnerReservationReminderEmail(Reservation reservation) throws MessagingException {
        User owner = getOwner(reservation);
        Context context = createReservationContext(reservation);
        context.setVariable("ownerName", owner.getName());

        sendEmail(owner.getEmail(), "Upcoming Reservation at Your Property", "ReminderOwnerReservation", context);
    }

    @Override
    public void sendCancellationConfirmationEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        Accommodation accommodation = reservation.getAccommodation();
        Context context = createCancellationContext(guest, accommodation, reservation);

        sendEmail(guest.getEmail(), "Your reservation has been successfully cancelled", "CancellationGuest", context);
    }

    @Override
    public void sendCancellationByOwnerNotificationEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        Accommodation accommodation = reservation.getAccommodation();
        Context context = createCancellationContext(guest, accommodation, reservation);

        sendEmail(guest.getEmail(), "Your reservation has been cancelled", "CancellationGuestByOwner", context);
    }

    @Override
    public void sendCancellationNotificationToOwnerEmail(Reservation reservation) throws MessagingException {
        User guest = getGuest(reservation);
        User owner = getOwner(reservation);
        Accommodation accommodation = reservation.getAccommodation();
        Context context = createCancellationContext(guest, accommodation, reservation);

        sendEmail(owner.getEmail(), "Reservation at your property has been cancelled", "CancellationByGuestOwner", context);
    }

    private Context createContext() {
        Context context = new Context();
        context.setVariable("dashboardUrl", DASHBOARD_URL);
        return context;
    }

    private Context createReservationContext(Reservation reservation) {
        Context context = createContext();
        Accommodation accommodation = reservation.getAccommodation();

        context.setVariable("accommodationName", accommodation.getName());
        context.setVariable("location", accommodation.getLocation());
        context.setVariable("checkInDate", reservation.getCheckInDate());
        context.setVariable("checkOutDate", reservation.getCheckOutDate());
        context.setVariable("amount", reservation.getTotalPrice());

        return context;
    }

    private Context createCancellationContext(User guest, Accommodation accommodation, Reservation reservation) {
        Context context = createContext();
        context.setVariable("userName", guest.getName());
        context.setVariable("accommodationName", accommodation.getName());
        context.setVariable("location", accommodation.getLocation());
        context.setVariable("checkInDate", reservation.getCheckInDate().toString());
        context.setVariable("checkOutDate", reservation.getCheckOutDate().toString());
        return context;
    }

    private User getOwner(Reservation reservation) {
        return reservation.getAccommodation().getManagedBy();
    }

    private User getGuest(Reservation reservation) {
        return reservation.getUser();
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
