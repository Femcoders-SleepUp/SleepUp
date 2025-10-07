package com.SleepUp.SU.utils.schedule;


import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.utils.Schedule.ReservationReminderTask;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ScheduleReservationReminderTaskTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailServiceHelper emailServiceHelper;

    @InjectMocks
    private ReservationReminderTask reservationReminderTask;

    private Reservation reservation1;
    private Reservation reservation2;
    private LocalDate reminderDate;

    @BeforeEach
    void setUp() {
        reminderDate = LocalDate.now().plusDays(3);

        User owner = new User();
        owner.setEmail("owner@test.com");

        User guest = new User();
        guest.setEmail("guest@test.com");

        Accommodation accommodation1 = new Accommodation();
        accommodation1.setName("Test Accommodation 1");
        accommodation1.setManagedBy(owner);

        Accommodation accommodation2 = new Accommodation();
        accommodation2.setName("Test Accommodation 2");
        accommodation2.setManagedBy(owner);

        reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setCheckInDate(reminderDate);
        reservation1.setAccommodation(accommodation1);
        reservation1.setUser(guest);

        reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setCheckInDate(reminderDate);
        reservation2.setAccommodation(accommodation2);
        reservation2.setUser(guest);
    }

    @Nested
    class sendReservationReminders {

        @Test
        void sendReservationReminders_withNoReservations_shouldLogAndReturn() {
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Collections.emptyList());

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailService, never()).sendGuestReservationReminderEmail(any());
            verify(emailService, never()).sendOwnerReservationReminderEmail(any());
        }

        @Test
        void sendReservationReminders_withValidReservations_shouldSendEmails() {
            List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(reservations);
            when(emailServiceHelper.canSendReservationEmails(any(Reservation.class)))
                    .thenReturn(true);

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailServiceHelper, times(2)).canSendReservationEmails(any(Reservation.class));
            verify(emailService, times(2)).sendGuestReservationReminderEmail(any(Reservation.class));
            verify(emailService, times(2)).sendOwnerReservationReminderEmail(any(Reservation.class));
        }

        @Test
        void sendReservationReminders_whenValidationFails_shouldNotSendEmails() {
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(Collections.singletonList(reservation1));
            when(emailServiceHelper.canSendReservationEmails(reservation1))
                    .thenReturn(false);

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailServiceHelper).canSendReservationEmails(reservation1);
            verify(emailService, never()).sendGuestReservationReminderEmail(any());
            verify(emailService, never()).sendOwnerReservationReminderEmail(any());
        }

        @Test
        void sendReservationReminders_whenEmailServiceThrowsException_shouldContinueProcessing() {
            List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(reservations);
            when(emailServiceHelper.canSendReservationEmails(any(Reservation.class)))
                    .thenReturn(true);
            doThrow(new RuntimeException("Email send failed"))
                    .when(emailService).sendGuestReservationReminderEmail(reservation1);

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(reminderDate);
            verify(emailService).sendGuestReservationReminderEmail(reservation1);
            verify(emailService).sendGuestReservationReminderEmail(reservation2);
            verify(emailService).sendOwnerReservationReminderEmail(reservation2);
        }

        @Test
        void sendReservationReminders_withMixedValidations_shouldProcessCorrectly() {
            List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
            when(reservationRepository.findByCheckInDate(reminderDate))
                    .thenReturn(reservations);
            when(emailServiceHelper.canSendReservationEmails(reservation1))
                    .thenReturn(true);
            when(emailServiceHelper.canSendReservationEmails(reservation2))
                    .thenReturn(false);

            reservationReminderTask.sendReservationReminders();

            verify(emailService, times(1)).sendGuestReservationReminderEmail(reservation1);
            verify(emailService, times(1)).sendOwnerReservationReminderEmail(reservation1);
            verify(emailService, never()).sendGuestReservationReminderEmail(reservation2);
            verify(emailService, never()).sendOwnerReservationReminderEmail(reservation2);
        }

        @Test
        void sendReservationReminders_whenRepositoryThrowsException_shouldHandleGracefully() {
            when(reservationRepository.findByCheckInDate(any(LocalDate.class)))
                    .thenThrow(new RuntimeException("Database error"));

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(any(LocalDate.class));
            verify(emailService, never()).sendGuestReservationReminderEmail(any());
            verify(emailService, never()).sendOwnerReservationReminderEmail(any());
        }

        @Test
        void sendReservationReminders_shouldCalculateCorrectReminderDate() {
            LocalDate expectedDate = LocalDate.now().plusDays(3);
            when(reservationRepository.findByCheckInDate(expectedDate))
                    .thenReturn(Collections.emptyList());

            reservationReminderTask.sendReservationReminders();

            verify(reservationRepository).findByCheckInDate(expectedDate);
        }
    }
}
