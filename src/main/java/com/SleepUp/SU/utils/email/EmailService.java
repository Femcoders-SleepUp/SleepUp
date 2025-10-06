package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;

import java.math.BigDecimal;

public interface EmailService {

    void sendWelcomeEmail(User user);

    void sendOwnerReservedNotification(Reservation reservation);

    void sendGuestReservationConfirmationEmail(Reservation reservation, BigDecimal discountAmount);

    void sendGuestReservationReminderEmail(Reservation reservation);

    void sendOwnerReservationReminderEmail(Reservation reservation);

    void sendCancellationConfirmationEmail(Reservation reservation);

    void sendCancellationByOwnerNotificationEmail(Reservation reservation);

    void sendCancellationNotificationToOwnerEmail(Reservation reservation);
}
