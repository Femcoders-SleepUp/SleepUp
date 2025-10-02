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
@ActiveProfiles("test") // application-test.yml should configure SMTP pointing to MailHog localhost:1025
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
        reservation.setUser(guest);
        reservation.setAccommodation(accommodation);
        reservation.setCheckInDate(LocalDate.now().plusDays(10));
        reservation.setCheckOutDate(LocalDate.now().plusDays(15));
        reservation.setTotalPrice(new BigDecimal("1200.00"));
    }

    @Test
    public void testSendWelcomeEmail() throws MessagingException, InterruptedException {
        emailService.sendWelcomeEmail(guest);
        Thread.sleep(1000); // wait for MailHog to receive email
    }

    @Test
    public void testSendOwnerReservedNotification() throws MessagingException, InterruptedException {
        emailService.sendOwnerReservedNotification(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void testSendGuestReservationConfirmationEmail() throws MessagingException, InterruptedException {
        emailService.sendGuestReservationConfirmationEmail(reservation, new BigDecimal("100.00"));
        Thread.sleep(1000);
    }

    @Test
    public void testSendGuestReservationReminderEmail() throws MessagingException, InterruptedException {
        emailService.sendGuestReservationReminderEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void testSendOwnerReservationReminderEmail() throws MessagingException, InterruptedException {
        emailService.sendOwnerReservationReminderEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void testSendCancellationConfirmationEmail() throws MessagingException, InterruptedException {
        emailService.sendCancellationConfirmationEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void testSendCancellationByOwnerNotificationEmail() throws MessagingException, InterruptedException {
        emailService.sendCancellationByOwnerNotificationEmail(reservation);
        Thread.sleep(1000);
    }

    @Test
    public void testSendCancellationNotificationToOwnerEmail() throws MessagingException, InterruptedException {
        emailService.sendCancellationNotificationToOwnerEmail(reservation);
        Thread.sleep(1000);
    }
}

//@Test
//void createReservation_validData_shouldReturnCreatedReservation() throws Exception {
//    ReservationRequest request = new ReservationRequest(
//            1,
//            LocalDate.of(2025, 10, 1),
//            LocalDate.of(2025, 10, 8)
//    );
//
//    Long accommodationId = 2L;
//    Accommodation accommodation = accommodationRepository.findById(accommodationId)
//            .orElseThrow(() -> new RuntimeException("Accommodation with id 2L not found"));
//
//    mockMvc.perform(post(ACCOMMODATIONS_PATH + "/{accommodationId}/reservations", accommodationId)
//                    .with(user(principal2))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(request)))
//            .andDo(print())
//            .andExpect(status().isCreated())
//            .andExpect(jsonPath("$.accommodationName").value(accommodation.getName()))
//            .andExpect(jsonPath("$.guestNumber").value(1))
//            .andExpect(jsonPath("$.bookingStatus").value("PENDING"))
//            .andExpect(jsonPath("$.userName").value(principal2.getUser().getName()));
//}
