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

@Service
public interface EmailService {
    void sendWelcomeEmail(String toEmail, String userName) throws MessagingException;

    void sendOwnerReservedNotification(User guest, Accommodation accommodation, Reservation reservation, double amount) throws MessagingException;

    void sendReservationConfirmationEmail(String toEmail, String userName, String accommodationName, String location, String checkInDate, String checkOutDate, double amount) throws MessagingException;

    void sendReservationReminderEmail(String toEmail, String guestName, String accommodationName, String location, String checkInDate, String checkOutDate) throws MessagingException;

    void sendOwnerReservationReminderEmail(String toEmail, String ownerName, String accommodationName,String location, String checkInDate, String checkOutDate) throws MessagingException;

    void sendCancellationConfirmationEmail(String toEmail, String userName, String accommodationName, String location, String checkInDate, String checkOutDate) throws MessagingException;

    void sendCancellationByOwnerNotificationEmail(String toEmail, String userName, String accommodationName, String location, String checkInDate, String checkOutDate) throws MessagingException;

    void sendCancellationNotificationToOwnerEmail(String toEmail, String userName, String accommodationName, String location, String checkInDate, String checkOutDate) throws MessagingException;

    void sendReservationConfirmationEmail(User guest, Accommodation accommodation, Reservation reservation, double amount) throws MessagingException;

    void sendReservationReminderEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException;

    void sendOwnerReservationReminderEmail(Accommodation accommodation, Reservation reservation) throws MessagingException;

    void sendCancellationConfirmationEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException;

    void sendCancellationByOwnerNotificationEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException;

    void sendCancellationNotificationToOwnerEmail(User guest, Accommodation accommodation, Reservation reservation) throws MessagingException;
}