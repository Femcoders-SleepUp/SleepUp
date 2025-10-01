package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface EmailService {
    void sendWelcomeEmail(User user) throws MessagingException;

    void sendOwnerReservedNotification(Reservation reservation) throws MessagingException;

    void sendGuestReservationConfirmationEmail(Reservation reservation, BigDecimal discountAmount) throws MessagingException;

    void sendReservationReminderEmail(Reservation reservation) throws MessagingException;

    void sendOwnerReservationReminderEmail(Reservation reservation) throws MessagingException;

    void sendCancellationConfirmationEmail(Reservation reservation) throws MessagingException;

    void sendCancellationByOwnerNotificationEmail(Reservation reservation) throws MessagingException;

    void sendCancellationNotificationToOwnerEmail(Reservation reservation) throws MessagingException;
}
