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

import java.time.Period;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService{

    private static final String UTF8_ENCODING = "UTF-8";
    private static final String DASHBOARD_URL = "http://localhost:8080/swagger-ui/index.html#/";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendWelcomeEmail(String toEmail, String userName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("dashboardUrl", DASHBOARD_URL);

        String htmlContent = templateEngine.process("WelcomeUser", context);

        helper.setTo(toEmail);
        helper.setSubject("Welcome to SleepUp!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Welcome email sent successfully to: {}", toEmail);
    }

    @Override
    public void sendOwnerReservedNotification(User guest, Accommodation accommodation, Reservation reservation, double amount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("ownerName", accommodation.getManagedBy().getName());
        context.setVariable("accommodationName", accommodation.getName());
        context.setVariable("location", accommodation.getLocation());
        context.setVariable("guestName", guest.getName());
        context.setVariable("checkInDate", reservation.getCheckInDate());
        context.setVariable("checkOutDate", reservation.getCheckOutDate());
        context.setVariable("amount", amount);
        context.setVariable("dashboardUrl", DASHBOARD_URL);

        String htmlContent = templateEngine.process("NotificationReservationOwner", context);

        helper.setTo(accommodation.getManagedBy().getEmail());
        helper.setSubject("Your property has just been booked!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Owner notification email sent successfully to: {}", accommodation.getManagedBy().getEmail());
    }

    @Override
    public void sendReservationConfirmationEmail(String toEmail, String userName, String accommodationName,
                                                 String location, String checkInDate, String checkOutDate, double amount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);
        context.setVariable("amount", amount);
        context.setVariable("dashboardUrl", DASHBOARD_URL);

        String htmlContent = templateEngine.process("ConfirmationGuest", context);

        helper.setTo(toEmail);
        helper.setSubject("Your Booking Confirmation!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Reservation confirmation email sent successfully to: {}", toEmail);
    }


    @Override
    public void sendReservationReminderEmail(String toEmail, String guestName, String accommodationName,
                                             String location, String checkInDate, String checkOutDate) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("guestName", guestName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);
        context.setVariable("dashboardUrl", DASHBOARD_URL);

        String htmlContent = templateEngine.process("ReminderGuestReservation", context);

        helper.setTo(toEmail);
        helper.setSubject("Upcoming Reservation Reminder");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Reservation reminder email sent successfully to: {}", toEmail);
    }

    @Override
    public void sendOwnerReservationReminderEmail(String toEmail, String ownerName, String accommodationName,
                                                  String location, String checkInDate, String checkOutDate) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("ownerName", ownerName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);
        context.setVariable("dashboardUrl", DASHBOARD_URL);

        String htmlContent = templateEngine.process("ReminderOwnerReservation", context);

        helper.setTo(toEmail);
        helper.setSubject("Upcoming Reservation at Your Property");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Owner reservation reminder email sent successfully to: {}", toEmail);
    }

    @Override
    public void sendCancellationConfirmationEmail(String toEmail, String userName, String accommodationName,
                                                  String location, String checkInDate, String checkOutDate) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);

        String htmlContent = templateEngine.process("CancellationGuest", context);

        helper.setTo(toEmail);
        helper.setSubject("Your reservation has been successfully cancelled");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Cancellation confirmation email sent successfully to: {}", toEmail);
    }

    @Override
    public void sendCancellationByOwnerNotificationEmail(String toEmail, String userName, String accommodationName,
                                                         String location, String checkInDate, String checkOutDate) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);

        String htmlContent = templateEngine.process("CancellationGuestByOwner", context);

        helper.setTo(toEmail);
        helper.setSubject("Your reservation has been cancelled");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Cancellation by owner notification email sent successfully to: {}", toEmail);
    }

    @Override
    public void sendCancellationNotificationToOwnerEmail(String toEmail, String userName, String accommodationName,
                                                         String location, String checkInDate, String checkOutDate) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF8_ENCODING);

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);

        String htmlContent = templateEngine.process("CancellationByGuestOwner", context);

        helper.setTo(toEmail);
        helper.setSubject("Reservation at your property has been cancelled");
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Cancellation notification to owner email sent successfully to: {}", toEmail);
    }


    @Override
    public void sendReservationConfirmationEmail(User guest, Accommodation accommodation, Reservation reservation, double amount) throws MessagingException {
        sendReservationConfirmationEmail(
                guest.getEmail(),
                guest.getName(),
                accommodation.getName(),
                accommodation.getLocation(),
                reservation.getCheckInDate().toString(),
                reservation.getCheckOutDate().toString(),
                amount
        );
    }

    @Override
    public void sendReservationReminderEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException {
        sendReservationReminderEmail(
                guest.getEmail(),
                guest.getName(),
                accommodation.getName(),
                accommodation.getLocation(),
                reservation.getCheckInDate().toString(),
                reservation.getCheckOutDate().toString()
        );
    }


    @Override
    public void sendOwnerReservationReminderEmail(Accommodation accommodation, Reservation reservation) throws MessagingException {
        User owner = accommodation.getManagedBy();
        sendOwnerReservationReminderEmail(
                owner.getEmail(),
                owner.getName(),
                accommodation.getName(),
                accommodation.getLocation(),
                reservation.getCheckInDate().toString(),
                reservation.getCheckOutDate().toString()
        );
    }


    @Override
    public void sendCancellationConfirmationEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException {
        sendCancellationConfirmationEmail(
                guest.getEmail(),
                guest.getName(),
                accommodation.getName(),
                accommodation.getLocation(),
                reservation.getCheckInDate().toString(),
                reservation.getCheckOutDate().toString()
        );
    }


    @Override
    public void sendCancellationByOwnerNotificationEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException {
        sendCancellationByOwnerNotificationEmail(
                guest.getEmail(),
                guest.getName(),
                accommodation.getName(),
                accommodation.getLocation(),
                reservation.getCheckInDate().toString(),
                reservation.getCheckOutDate().toString()
        );
    }


    @Override
    public void sendCancellationNotificationToOwnerEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException {
        User owner = accommodation.getManagedBy();
        sendCancellationNotificationToOwnerEmail(
                owner.getEmail(),
                guest.getName(),
                accommodation.getName(),
                accommodation.getLocation(),
                reservation.getCheckInDate().toString(),
                reservation.getCheckOutDate().toString()
        );
    }
}
