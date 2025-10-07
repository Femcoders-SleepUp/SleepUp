package com.SleepUp.SU.security.jwt;

import com.SleepUp.SU.config.properties.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        AppProperties.JwtProperties jwtProperties = new AppProperties.JwtProperties();
        jwtProperties.setSecret("YourBase64EncodedSecretHereYourBase64EncodedSecretHere");
        jwtProperties.setExpirationMs(60000L);
        jwtProperties.setRefreshExpirationMs(120000L);

        AppProperties appProperties = new AppProperties();
        appProperties.setJwt(jwtProperties);

        jwtService = new JwtService(appProperties);
    }

    @Test
    void isValidToken_isTokenExpiredThrows_shouldReturnFalse() {
        JwtService spyJwtService = spy(jwtService);
        String token = "dummyToken";

        doThrow(new RuntimeException("expired token")).when(spyJwtService).isTokenExpired(token);

        boolean result = spyJwtService.isValidToken(token);
        assertFalse(result);
    }

    @Test
    void refreshAccessToken_tokenInvalid_shouldThrowRuntimeException() {
        String refreshToken = "invalidToken";
        JwtService spyJwtService = spy(jwtService);

        doReturn(false).when(spyJwtService).isValidToken(refreshToken);

        Exception exception = assertThrows(RuntimeException.class,
                () -> spyJwtService.refreshAccessToken(refreshToken, userDetails));

        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    @Test
    void refreshAccessToken_usernameMismatch_shouldThrowRuntimeException() {
        String refreshToken = "validToken";

        JwtService spyJwtService = spy(jwtService);
        when(userDetails.getUsername()).thenReturn("user1");

        doReturn(true).when(spyJwtService).isValidToken(refreshToken);
        doReturn("differentUser").when(spyJwtService).extractUsername(refreshToken);

        Exception exception = assertThrows(RuntimeException.class,
                () -> spyJwtService.refreshAccessToken(refreshToken, userDetails));

        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }
}