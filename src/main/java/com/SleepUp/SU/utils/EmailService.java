package com.SleepUp.SU.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String UTF8_ENCODING = "UTF-8";
    private static final String DASHBOARD_URL = "http://localhost:8080/swagger-ui/index.html#/";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

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
    }

    public void sendReservationConfirmationEmail(String toEmail, String userName,  String accommodationName, String location, String checkInDate, String checkOutDate) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("accommodationName", accommodationName);
        context.setVariable("location", location);
        context.setVariable("checkInDate", checkInDate);
        context.setVariable("checkOutDate", checkOutDate);
        context.setVariable("dashboardUrl", DASHBOARD_URL);


        String htmlContent = templateEngine.process("ConfirmationGuest", context);

        helper.setTo(toEmail);
        helper.setSubject("Your Booking Confirmation!!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendReservationReminderEmail(String toEmail, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("ReminderGuestReservation", context);

        helper.setTo(toEmail);
        helper.setSubject("Upcoming Reservation Reminder");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendCancellationConfirmationEmail(String toEmail, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("CancellationGuest", context);

        helper.setTo(toEmail);
        helper.setSubject("Your reservation has been successfully cancelled");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendOwnerReservedNotification(String toEmail, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("NotificationReservationOwner", context);

        helper.setTo(toEmail);
        helper.setSubject("Your property has just been booked!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

}
