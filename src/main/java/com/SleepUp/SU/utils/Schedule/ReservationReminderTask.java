package com.SleepUp.SU.utils.Schedule;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationReminderTask {

    private static final Logger log = LoggerFactory.getLogger(ReservationReminderTask.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int REMINDER_DAYS_BEFORE = 3;

    private final ReservationRepository reservationRepository;
    private final EmailServiceHelper emailServiceHelper;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendReservationReminders() {
        log.info("Starting daily reservation reminder task at {}", LocalDate.now().format(DATE_FORMATTER));

        try {
            LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE);
            log.info("Looking for reservations starting on: {}", reminderDate.format(DATE_FORMATTER));

            List<Reservation> upcomingReservations = findReservationsStartingOn(reminderDate);

            if (upcomingReservations.isEmpty()) {
                log.info("No reservations found for reminder date: {}", reminderDate.format(DATE_FORMATTER));
                return;
            }

            log.info("Found {} reservations requiring reminders", upcomingReservations.size());

            int successCount = 0;
            int failureCount = 0;

            for (Reservation reservation : upcomingReservations) {
                try {
                    if (emailServiceHelper.canSendReservationEmails(
                            reservation.getAccommodation().getManagedBy(),
                            reservation.getAccommodation(),
                            reservation)) {

                        emailServiceHelper.sendReservationReminders(
                                reservation.getAccommodation().getManagedBy(),
                                reservation.getAccommodation(),
                                reservation
                        );

                        emailServiceHelper.sendOwnerReservationReminderEmail(
                                reservation.getAccommodation(),
                                reservation);

                        successCount++;
                        log.debug("Reminder sent successfully for reservation ID: {} - Guest: {}, Accommodation: {}",
                                reservation.getId(),
                                reservation.getAccommodation().getManagedBy().getEmail(),
                                reservation.getAccommodation().getName());

                    } else {
                        failureCount++;
                        log.warn("Cannot send reminder for reservation ID: {} - Validation failed",
                                reservation.getId());
                    }

                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to send reminder for reservation ID: {} - Error: {}",
                            reservation.getId(), e.getMessage(), e);
                }

                Thread.sleep(100);
            }

            log.info("Reservation reminder task completed. Success: {}, Failures: {}",
                    successCount, failureCount);

        } catch (Exception e) {
            log.error("Critical error in reservation reminder task: {}", e.getMessage(), e);
        }
    }

    private List<Reservation> findReservationsStartingOn(LocalDate date) {
        return reservationRepository.findByCheckInDate(date);
    }
}