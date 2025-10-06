package com.SleepUp.SU.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.utils.email.EmailServiceImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceMailHogIntegrationTest {

    @Autowired
    private EmailServiceImpl emailService;

    private User owner;
    private User guest;
    private Accommodation accommodation;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner User");
        owner.setEmail("sleepup.backend@gmail.com");

        guest = new User();
        guest.setName("Guest User");
        guest.setEmail("sleepup.backend@gmail.com");

        accommodation = new Accommodation();
        accommodation.setName("Seaside Villa");
        accommodation.setLocation("Beach City");
        accommodation.setManagedBy(owner);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(guest);
        reservation.setAccommodation(accommodation);
        reservation.setCheckInDate(LocalDate.now().plusDays(10));
        reservation.setCheckOutDate(LocalDate.now().plusDays(15));
        reservation.setTotalPrice(new BigDecimal("1200.00"));
    }

    @Test
    public void integration_sendWelcomeEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendWelcomeEmail(guest);
        Thread.sleep(1000);
    }

    @Test
    public void integration_sendOwnerReservedNotification_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendOwnerReservedNotification(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void integration_sendGuestReservationConfirmationEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendGuestReservationConfirmationEmail(reservation, new BigDecimal("100.00"));
        Thread.sleep(1000);
    }

    @Test
    public void integration_sendGuestReservationReminderEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendGuestReservationReminderEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void integration_sendOwnerReservationReminderEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendOwnerReservationReminderEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void  integration_sendCancellationConfirmationEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendCancellationConfirmationEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void  integration_sendCancellationByOwnerNotificationEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendCancellationByOwnerNotificationEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void integration_sendCancellationNotificationToOwnerEmail_shouldNotThrowException() throws MessagingException, InterruptedException {
        emailService.sendCancellationNotificationToOwnerEmail(reservation);
        Thread.sleep(1000);
    }
}
