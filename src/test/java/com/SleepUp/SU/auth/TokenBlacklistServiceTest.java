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
    void when_addTokenToBlacklist_withValidToken() {
        String token = "valid-token";
        Date expiration = new Date(System.currentTimeMillis() + 10000); 
        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token);

        assertTrue(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void when_addTokenToBlacklist_withInvalidToken_throw_exception() {
        String token = "invalid-token";
        when(jwtService.extractExpiration(token)).thenThrow(new RuntimeException("Invalid token"));

        tokenBlacklistService.addToBlacklist(token);

        assertTrue(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void when_returnFalse_when_tokenNotInBlacklist() {
        assertFalse(tokenBlacklistService.isTokenInBlacklist("not-exists-token"));
    }

    @Test
    void when_returnFalseAndRemoveToken_when_tokenExpired() {
        String token = "expired-token";
        Date expiration = new Date(System.currentTimeMillis() - 1000); 
        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token);

        assertFalse(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void when_removeTokenFromBlacklist() {
        String token = "remove-token";
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token);
        assertTrue(tokenBlacklistService.isTokenInBlacklist(token));

        tokenBlacklistService.removeFromBlacklist(token);

        assertFalse(tokenBlacklistService.isTokenInBlacklist(token));
    }

    @Test
    void when_removeExpiredTokens_scheduledTask() {
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
    void when_returnCorrectBlacklistedTokensCount() {
        String token1 = "token1";
        String token2 = "token2";
        Date expiration = new Date(System.currentTimeMillis() + 10000);

        when(jwtService.extractExpiration(anyString())).thenReturn(expiration);

        tokenBlacklistService.addToBlacklist(token1);
        tokenBlacklistService.addToBlacklist(token2);

        assertEquals(2, tokenBlacklistService.getBlacklistedTokensCount());
    }
}
