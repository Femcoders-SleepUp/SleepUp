package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.auth.AuthService;
import com.SleepUp.SU.reservation.Reservation;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceHelper {

    private final EmailService emailService;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public void sendWelcomeEmail(UserRequest userRequest, User user) {
        try {
            emailService.sendWelcomeEmail(userRequest.email(), userRequest.username());
            logger.info("Welcome email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendOwnerReservedNotification(User user, Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendOwnerReservedNotification(user, accommodation, reservation);
            logger.info("New reservation sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send new reservation email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public void sendReservationConfirmationEmail(User guest, Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendReservationConfirmationEmail(guest, accommodation, reservation);
            logger.info("Reservation confirmation email sent successfully to: {}", guest.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send reservation confirmation email to {}: {}", guest.getEmail(), e.getMessage());
        }
    }

    public void sendReservationReminderEmail(User guest, Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendReservationReminderEmail(guest, accommodation, reservation);
            logger.info("Reservation reminder email sent successfully to: {}", guest.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send reservation reminder email to {}: {}", guest.getEmail(), e.getMessage());
        }
    }

    public void sendOwnerReservationReminderEmail(Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendOwnerReservationReminderEmail(accommodation, reservation);
            logger.info("Owner reservation reminder email sent successfully to: {}",
                    accommodation.getManagedBy().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send owner reservation reminder email to {}: {}",
                    accommodation.getManagedBy().getEmail(), e.getMessage());
        }
    }

    public void sendCancellationConfirmationEmail(User guest, Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendCancellationConfirmationEmail(guest, accommodation, reservation);
            logger.info("Cancellation confirmation email sent successfully to: {}", guest.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send cancellation confirmation email to {}: {}", guest.getEmail(), e.getMessage());
        }
    }

    public void sendCancellationByOwnerNotificationEmail(User guest, Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendCancellationByOwnerNotificationEmail(guest, accommodation, reservation);
            logger.info("Cancellation by owner notification email sent successfully to: {}", guest.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send cancellation by owner notification email to {}: {}",
                    guest.getEmail(), e.getMessage());
        }
    }

    public void sendCancellationNotificationToOwnerEmail(User guest, Accommodation accommodation, Reservation reservation) {
        try {
            emailService.sendCancellationNotificationToOwnerEmail(guest, accommodation, reservation);
            logger.info("Cancellation notification to owner email sent successfully to: {}",
                    accommodation.getManagedBy().getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send cancellation notification to owner email to {}: {}",
                    accommodation.getManagedBy().getEmail(), e.getMessage());
        }
    }

    public void handleNewReservationEmails(User guest, Accommodation accommodation, Reservation reservation) {
        logger.info("Processing emails for new reservation - Guest: {}, Accommodation: {}",
                guest.getEmail(), accommodation.getName());

        sendReservationConfirmationEmail(guest, accommodation, reservation);
        sendOwnerReservedNotification(guest, accommodation, reservation);

        logger.info("New reservation email processing completed");
    }

    public void handleGuestCancellationEmails(User guest, Accommodation accommodation, Reservation reservation) {
        logger.info("Processing emails for guest cancellation - Guest: {}, Accommodation: {}",
                guest.getEmail(), accommodation.getName());

        sendCancellationConfirmationEmail(guest, accommodation, reservation);
        sendCancellationNotificationToOwnerEmail(guest, accommodation, reservation);

        logger.info("Guest cancellation email processing completed");
    }

    public void handleOwnerCancellationEmails(User guest, Accommodation accommodation, Reservation reservation) {
        logger.info("Processing emails for owner cancellation - Guest: {}, Accommodation: {}",
                guest.getEmail(), accommodation.getName());

        sendCancellationByOwnerNotificationEmail(guest, accommodation, reservation);

        logger.info("Owner cancellation email processing completed");
    }

    public void sendReservationReminders(User guest, Accommodation accommodation, Reservation reservation) {
        logger.info("Sending reservation reminders - Guest: {}, Owner: {}, Accommodation: {}",
                guest.getEmail(), accommodation.getManagedBy().getEmail(), accommodation.getName());

        sendReservationReminderEmail(guest, accommodation, reservation);
        sendOwnerReservationReminderEmail(accommodation, reservation);

        logger.info("Reservation reminders sent successfully");
    }

    public boolean canSendEmails(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.warn("Cannot send email: User or email is null/empty");
            return false;
        }
        return true;
    }

    public boolean canSendReservationEmails(User guest, Accommodation accommodation, Reservation reservation) {
        if (!canSendEmails(guest)) {
            return false;
        }

        if (accommodation == null || accommodation.getManagedBy() == null) {
            logger.warn("Cannot send reservation emails: Accommodation or owner is null");
            return false;
        }

        if (!canSendEmails(accommodation.getManagedBy())) {
            logger.warn("Cannot send emails to owner: Owner email is null/empty");
            return false;
        }

        if (reservation == null) {
            logger.warn("Cannot send reservation emails: Reservation is null");
            return false;
        }

        return true;
    }
}
