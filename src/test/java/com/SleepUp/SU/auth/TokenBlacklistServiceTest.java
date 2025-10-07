package com.SleepUp.SU.auth;

import com.SleepUp.SU.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private JwtService jwtService;

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(jwtService);
    }

    @Test
    void addToBlacklist_validToken_shouldBeInBlacklist() {
        String token = "valid-token";
        Date expiration = new Date(System.currentTimeMillis() + 10000); 
        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token);

        assertTrue(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void addToBlacklist_invalidToken_shouldStillBeInBlacklist() {
        String token = "invalid-token";
        when(jwtService.extractExpiration(token)).thenThrow(new RuntimeException("Invalid token"));

        tokenBlacklistService.addToBlacklist(token);

        assertTrue(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void isTokenInBlacklist_tokenNotInBlacklist_shouldReturnFalse() {
        assertFalse(tokenBlacklistService.isTokenInBlacklist("not-exists-token"));
    }

    @Test
    void isTokenInBlacklist_expiredToken_shouldReturnFalseAndRemoveToken() {
        String token = "expired-token";
        Date expiration = new Date(System.currentTimeMillis() - 1000); 
        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token);

        assertFalse(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void removeFromBlacklist_existingToken_shouldRemoveToken() {
        String token = "remove-token";
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token);
        assertTrue(tokenBlacklistService.isTokenInBlacklist(token));

        tokenBlacklistService.removeFromBlacklist(token);

        assertFalse(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void removeExpiredTokens_taskExecuted_shouldRemoveExpiredAndKeepActiveTokens() {
        String expiredToken = "expired-token";
        String activeToken = "active-token";

        Date expiredDate = new Date(System.currentTimeMillis() - 1000);
        Date activeDate = new Date(System.currentTimeMillis() + 10000);

        when(jwtService.extractExpiration(expiredToken)).thenReturn(expiredDate);
        when(jwtService.extractExpiration(activeToken)).thenReturn(activeDate);

        tokenBlacklistService.addToBlacklist(expiredToken);
        tokenBlacklistService.addToBlacklist(activeToken);

        tokenBlacklistService.removeExpiredTokens();

        assertFalse(tokenBlacklistService.isTokenInBlacklist(expiredToken));
        assertTrue(tokenBlacklistService.isTokenInBlacklist(activeToken));
    }

    @Test
    void getBlacklistedTokensCount_multipleActiveTokens_shouldReturnCorrectCount() {
        String token1 = "token1";
        String token2 = "token2";
        Date expiration = new Date(System.currentTimeMillis() + 10000);

        when(jwtService.extractExpiration(anyString())).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token1);
        tokenBlacklistService.addToBlacklist(token2);

        assertEquals(2, tokenBlacklistService.getBlacklistedTokensCount());
    }
}