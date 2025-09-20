package com.SleepUp.SU.utils;

import com.SleepUp.SU.accommodation.Accommodation;
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
}
