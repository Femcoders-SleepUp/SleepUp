package com.SleepUp.SU.auth;

import com.SleepUp.SU.utils.ApiMessageDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceHelperTest {
    @InjectMocks
    private AuthServiceHelper authServiceHelper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void when_generateAccessToken_return_accessToken() {
        Claims claims = Jwts.claims()
                .add("username", "user")
                .build();

        String token = authServiceHelper.generateAccessToken("user", claims);

        assertNotNull(token);
        assertTrue(token.startsWith("ey"));
    }

    @Test
    void when_generateRefreshToken_return_refreshToken() {
        String refreshToken = authServiceHelper.generateRefreshToken("user");

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 100);
    }

    @Test
    void when_validateAccessToken_return_username() {
        Claims claims = Jwts.claims()
                .add("username", "user")
                .build();

        String token = authServiceHelper.generateAccessToken("user", claims);
        Claims claimsResult = authServiceHelper.validateAccessToken(token);

        assertEquals("user", claimsResult.getSubject());
    }

    @Test
    void when_validateRefreshToken_return_username() {
        String refreshToken = authServiceHelper.generateRefreshToken("user");
        Claims claimsResult = authServiceHelper.validateRefreshToken(refreshToken);

        assertEquals("user", claimsResult.getSubject());
    }

    @Nested
    class handleRefreshTokenTest {
        @Test
        void when_handleRefreshTokenTest_return_valid() {
            String refreshToken = authServiceHelper.generateRefreshToken("user");
            ResponseEntity<Map<String, String>> response = authServiceHelper.handleRefreshToken(refreshToken);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().get("accessToken"));
        }

        @Test
        void when_handleRefreshTokenTest_return_invalid() {
            ResponseEntity<Map<String, String>> response = authServiceHelper.handleRefreshToken("invalid-refresh-token");

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertEquals("Refresh invalid token", response.getBody().get("error"));

        }

        @Test
        void when_handleRefreshTokenIsBlank_return_noRefreshTokenProvided() {
            ResponseEntity<Map<String, String>> response = authServiceHelper.handleRefreshToken("");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("No refresh token provided", response.getBody().get("error"));
        }

        @Test
        void when_handleRefreshTokenIsNull_return_noRefreshTokenProvided() {
            ResponseEntity<Map<String, String>> response = authServiceHelper.handleRefreshToken(null);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("No refresh token provided", response.getBody().get("error"));
        }
    }

    @Test
    void when_getAuthentication_with_validToken_return_authentication() {
        Claims claims = Jwts.claims()
                .add("role", "USER")
                .build();
        String token = authServiceHelper.generateAccessToken("testUser", claims);

        Authentication authentication = authServiceHelper.getAuthentication(token);

        assertNotNull(authentication);
        assertEquals("testUser", authentication.getName());
        assertEquals("testUser", authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertEquals(1, authentication.getAuthorities().size());
    }

    @Test
    void when_getAuthentication_with_invalidToken_throw_runtimeException() {
        String invalidToken = "invalid.jwt.token";

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authServiceHelper.getAuthentication(invalidToken));

        assertEquals("Invalid token", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void when_getAuthentication_with_nullToken_throw_runtimeException() {
        assertThrows(RuntimeException.class,
                () -> authServiceHelper.getAuthentication(null));
    }

}
