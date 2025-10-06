package com.SleepUp.SU.utils.email;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

@Component
public class EmailServiceHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceHelper.class);

    private static final String DASHBOARD_URL = "http://localhost:8080/swagger-ui/index.html#";
    private static final String RESERVATIONS_URL = DASHBOARD_URL + "/api/v1/reservations";
    private static final String ACCOMMODATIONS_URL = DASHBOARD_URL + "/api/v1/accommodations";

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

    public Context createFullContext(Reservation reservation, User user, BigDecimal discountAmount) {
        Context context = new Context();

        context.setVariable("dashboardUrl", DASHBOARD_URL);

        if (reservation != null) {
            Accommodation accommodation = reservation.getAccommodation();
            context.setVariable("reservationUrl", setReservationUrl(reservation));
            context.setVariable("accommodationUrl", setAccommodationUrl(accommodation));
            context.setVariable("accommodationName", accommodation.getName());
            context.setVariable("location", accommodation.getLocation());
            context.setVariable("checkInDate", reservation.getCheckInDate() != null ? reservation.getCheckInDate().toString() : "N/A");
            context.setVariable("checkOutDate", reservation.getCheckOutDate() != null ? reservation.getCheckOutDate().toString() : "N/A");
            context.setVariable("amount", reservation.getTotalPrice());
        } else {
            context.setVariable("accommodationName", "Default Accommodation");
            context.setVariable("location", "Default Location");
            context.setVariable("checkInDate", "N/A");
            context.setVariable("checkOutDate", "N/A");
            context.setVariable("amount", "N/A");
        }

        if (user != null) {
            context.setVariable("userName", user.getName());
            context.setVariable("guestName", user.getName());
        } else {
            context.setVariable("userName", "Default User");
            context.setVariable("guestName", "Unknown Guest");
        }

        context.setVariable("discountAmount", discountAmount);

        return context;
    }

    public String setReservationUrl(Reservation reservation) {
        return RESERVATIONS_URL + "/" + reservation.getId();
    }

    public String setAccommodationUrl(Accommodation accommodation) {
        return ACCOMMODATIONS_URL + "/" + accommodation.getId();
    }

    public String getDashboardUrl() {
        return DASHBOARD_URL;
    }
}
