package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.auth.AuthService;
import com.SleepUp.SU.config.properties.MailProperties;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailServiceHelper {

    private final EmailService emailService;
    public Logger logger = LoggerFactory.getLogger(AuthService.class);

    public MailProperties mailProperties;
    private boolean emailEnabled;

    @PostConstruct
    public void validateEmailConfig() {
        emailEnabled = mailProperties != null
                && mailProperties.getFrom() != null && !mailProperties.getFrom().isBlank()
                && mailProperties.getUsername() != null && !mailProperties.getUsername().isBlank()
                && mailProperties.getPassword() != null && !mailProperties.getPassword().isBlank();

        if (!emailEnabled) {
            logger.warn("Email sending is disabled due to missing or invalid mail configuration.");
        } else {
            logger.info("Email configuration validated successfully. Email sending enabled.");
        }
    }

    public void sendWelcomeEmail(User user) {
        try {
            emailService.sendWelcomeEmail(user);
            logger.info("Welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendOwnerReservedNotification(Reservation reservation) {
        try {
            emailService.sendOwnerReservedNotification(reservation);
            logger.info("New reservation notification email sent successfully to: {}", reservation.getAccommodation().getManagedBy().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send new reservation email to {}: {}", reservation.getAccommodation().getManagedBy().getEmail(), e.getMessage());
        }
    }

    public void sendReservationConfirmationEmail(Reservation reservation, BigDecimal discountAmount) {
        try {
            emailService.sendGuestReservationConfirmationEmail(reservation, discountAmount);
            logger.info("Reservation confirmation email sent successfully to: {}", reservation.getUser().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send reservation confirmation email to {}: {}", reservation.getUser().getEmail(), e.getMessage());
        }
    }

    public void sendReservationReminderEmail(Reservation reservation) {
        try {
            emailService.sendGuestReservationReminderEmail(reservation);
            logger.info("Reservation reminder email sent successfully to: {}", reservation.getUser().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send reservation reminder email to {}: {}", reservation.getUser().getEmail(), e.getMessage());
        }
    }

    public void sendOwnerReservationReminderEmail(Reservation reservation) {
        try {
            emailService.sendOwnerReservationReminderEmail(reservation);
            logger.info("Owner reservation reminder email sent successfully to: {}", reservation.getAccommodation().getManagedBy().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send owner reservation reminder email to {}: {}", reservation.getAccommodation().getManagedBy().getEmail(), e.getMessage());
        }
    }

    public void sendCancellationConfirmationEmail(Reservation reservation) {
        try {
            emailService.sendCancellationConfirmationEmail(reservation);
            logger.info("Cancellation confirmation email sent successfully to: {}", reservation.getUser().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send cancellation confirmation email to {}: {}", reservation.getUser().getEmail(), e.getMessage());
        }
    }

    public void sendCancellationByOwnerNotificationEmail(Reservation reservation) {
        try {
            emailService.sendCancellationByOwnerNotificationEmail(reservation);
            logger.info("Cancellation by owner notification email sent successfully to: {}", reservation.getUser().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send cancellation by owner notification email to {}: {}", reservation.getUser().getEmail(), e.getMessage());
        }
    }

    public void sendCancellationNotificationToOwnerEmail(Reservation reservation) {
        try {
            emailService.sendCancellationNotificationToOwnerEmail(reservation);
            logger.info("Cancellation notification to owner email sent successfully to: {}", reservation.getAccommodation().getManagedBy().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send cancellation notification to owner email to {}: {}", reservation.getAccommodation().getManagedBy().getEmail(), e.getMessage());
        }
    }

    public void handleNewReservationEmails(Reservation reservation, BigDecimal discountAmount) {
        logger.info("Processing emails for new reservation - Guest: {}, Accommodation: {}",
                reservation.getUser().getEmail(), reservation.getAccommodation().getName());

        sendReservationConfirmationEmail(reservation, discountAmount);
        sendOwnerReservedNotification(reservation);

        logger.info("New reservation email processing completed");
    }

    public void handleGuestCancellationEmails(Reservation reservation) {
        logger.info("Processing emails for guest cancellation - Guest: {}, Accommodation: {}",
                reservation.getUser().getEmail(), reservation.getAccommodation().getName());

        sendCancellationConfirmationEmail(reservation);
        sendCancellationNotificationToOwnerEmail(reservation);

        logger.info("Guest cancellation email processing completed");
    }

    public void handleOwnerCancellationEmails(Reservation reservation) {
        logger.info("Processing emails for owner cancellation - Guest: {}, Accommodation: {}",
                reservation.getUser().getEmail(), reservation.getAccommodation().getName());

        sendCancellationByOwnerNotificationEmail(reservation);

        logger.info("Owner cancellation email processing completed");
    }

    public void sendReservationReminders(Reservation reservation) {
        logger.info("Sending reservation reminders - Guest: {}, Owner: {}, Accommodation: {}",
                reservation.getUser().getEmail(), reservation.getAccommodation().getManagedBy().getEmail(), reservation.getAccommodation().getName());

        sendReservationReminderEmail(reservation);
        sendOwnerReservationReminderEmail(reservation);

        logger.info("Reservation reminders sent successfully");
    }

    public boolean canSendEmails(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.warn("Cannot send email: User or email is null/empty");
            return false;
        }
        return true;
    }

    public boolean canSendReservationEmails(Reservation reservation) {
        if (reservation == null) {
            logger.warn("Cannot send reservation emails: Reservation is null");
            return false;
        }

        if (!canSendEmails(reservation.getUser())) {
            return false;
        }

        Accommodation accommodation = reservation.getAccommodation();
        if (accommodation == null || accommodation.getManagedBy() == null) {
            logger.warn("Cannot send reservation emails: Accommodation or owner is null");
            return false;
        }

        if (!canSendEmails(accommodation.getManagedBy())) {
            logger.warn("Cannot send emails to owner: Owner email is null/empty");
            return false;
        }

        return true;
    }
}
