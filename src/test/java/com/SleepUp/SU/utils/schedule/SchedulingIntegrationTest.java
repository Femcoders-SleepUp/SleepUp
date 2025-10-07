package com.SleepUp.SU.utils.schedule;


import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.Schedule.ReservationReminderTask;
import com.SleepUp.SU.utils.Schedule.ScheduledTasks;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        ReservationReminderTask.class,
        ScheduledTasks.class
})
@EnableScheduling
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
class SchedulingIntegrationTest {

    @Autowired
    private ReservationReminderTask reservationReminderTask;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private EmailServiceHelper emailServiceHelper;

    private Reservation testReservation;
    private LocalDate reminderDate;

    @BeforeEach
    void setUp() {
        reminderDate = LocalDate.now().plusDays(3);

        User owner = new User();
        owner.setEmail("owner@integration-test.com");

        User guest = new User();
        guest.setEmail("guest@integration-test.com");

        Accommodation accommodation = new Accommodation();
        accommodation.setName("Integration Test Accommodation");
        accommodation.setManagedBy(owner);

        testReservation = new Reservation();
        testReservation.setId(100L);
        testReservation.setCheckInDate(reminderDate);
        testReservation.setAccommodation(accommodation);
        testReservation.setUser(guest);
    }

    @Nested
    class reservationReminderTask {

        @Test
        void reservationReminderTask_integrationTest_withReservations() {
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Arrays.asList(testReservation));
            when(emailServiceHelper.canSendReservationEmails(any(Reservation.class)))
                    .thenReturn(true);

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailServiceHelper).canSendReservationEmails(testReservation);
            verify(emailService).sendGuestReservationReminderEmail(testReservation);
            verify(emailService).sendOwnerReservationReminderEmail(testReservation);
        }

        @Test
        void reservationReminderTask_integrationTest_withNoReservations() {
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Collections.emptyList());

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailService, never()).sendGuestReservationReminderEmail(any());
            verify(emailService, never()).sendOwnerReservationReminderEmail(any());
        }

        @Test
        void reservationReminderTask_integrationTest_withEmailFailure() {
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Arrays.asList(testReservation));
            when(emailServiceHelper.canSendReservationEmails(any(Reservation.class)))
                    .thenReturn(true);
            doThrow(new RuntimeException("SMTP connection failed"))
                    .when(emailService).sendGuestReservationReminderEmail(any());

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailService).sendGuestReservationReminderEmail(testReservation);
        }

        @Test
        void reservationReminderTask_integrationTest_withMultipleReservations() {
            Reservation reservation2 = new Reservation();
            reservation2.setId(101L);
            reservation2.setCheckInDate(reminderDate);
            reservation2.setAccommodation(testReservation.getAccommodation());
            reservation2.setUser(testReservation.getUser());

            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Arrays.asList(testReservation, reservation2));
            when(emailServiceHelper.canSendReservationEmails(any(Reservation.class)))
                    .thenReturn(true);

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailService, times(2)).sendGuestReservationReminderEmail(any());
            verify(emailService, times(2)).sendOwnerReservationReminderEmail(any());
        }

        @Test
        void reservationReminderTask_integrationTest_withPartialValidation() {
            Reservation reservation2 = new Reservation();
            reservation2.setId(102L);
            reservation2.setCheckInDate(reminderDate);
            reservation2.setAccommodation(testReservation.getAccommodation());
            reservation2.setUser(testReservation.getUser());

            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Arrays.asList(testReservation, reservation2));
            when(emailServiceHelper.canSendReservationEmails(testReservation))
                    .thenReturn(true);
            when(emailServiceHelper.canSendReservationEmails(reservation2))
                    .thenReturn(false);

            reservationReminderTask.sendReservationReminders();

            verify(emailService, times(1)).sendGuestReservationReminderEmail(testReservation);
            verify(emailService, times(1)).sendOwnerReservationReminderEmail(testReservation);
            verify(emailService, never()).sendGuestReservationReminderEmail(reservation2);
            verify(emailService, never()).sendOwnerReservationReminderEmail(reservation2);
        }
    }

    @Nested
    class scheduledTasks {

        @Test
        void scheduledTasks_integrationTest_allTasksExecute() {
            scheduledTasks.dailyMaintenanceTask();
            scheduledTasks.hourlySystemCheck();
            scheduledTasks.weeklyReportTask();
        }
    }
}
