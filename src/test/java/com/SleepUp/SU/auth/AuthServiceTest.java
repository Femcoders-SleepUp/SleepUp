package com.SleepUp.SU.auth;

import com.SleepUp.SU.auth.dto.AuthResponse;
import com.SleepUp.SU.auth.dto.LoginRequest;
import com.SleepUp.SU.auth.dto.RefreshRequest;
import com.SleepUp.SU.security.jwt.JwtService;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserServiceHelper userServiceHelper;

    @Mock
    private EmailServiceHelper emailService;

    @Nested
    class RegisterNewUserTest {

        @Test
        void register_validRequest_shouldReturnUserResponse() throws MessagingException {
            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");

            User userSaved = new User();
            userSaved.setId(1L);
            userSaved.setUsername("userTest");
            userSaved.setName("nameTest");
            userSaved.setEmail("usertest@test.com");
            userSaved.setPassword("password123");
            userSaved.setRole(Role.USER);

            when(userServiceHelper.createUser(userRequest, Role.USER)).thenReturn(userSaved);

            UserResponse userResponseMock = new UserResponse(
                    userSaved.getId(),
                    userSaved.getUsername(),
                    userSaved.getName(),
                    userSaved.getEmail(),
                    userSaved.getRole()
            );
            when(userMapper.toResponse(userSaved)).thenReturn(userResponseMock);

            UserResponse userResponse = authService.register(userRequest);

            assertEquals("userTest", userResponse.username());
            assertEquals("usertest@test.com", userResponse.email());
            verify(emailService).sendWelcomeEmail(userSaved);
        }

    }

    @Nested
    class LoginTest {

        @Test
        void login_validCredentials_shouldReturnAuthResponse() {
            LoginRequest loginRequest = new LoginRequest("userTest", "password123");

            Authentication mockAuthentication = mock(Authentication.class);
            CustomUserDetails mockCustomUserDetails = mock(CustomUserDetails.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(mockAuthentication.getPrincipal()).thenReturn(mockCustomUserDetails);
            when(jwtService.generateAccessToken(mockCustomUserDetails)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(mockCustomUserDetails)).thenReturn("refresh-token");

            AuthResponse response = authService.login(loginRequest);

            assertEquals("login", response.message());
            assertEquals("Bearer", response.tokenType());
            assertEquals("access-token", response.token());
            assertEquals("userTest", response.username());
            assertEquals("refresh-token", response.refreshToken());
        }

        @Test
        void login_invalidCredentials_shouldThrowAuthenticationException() {
            LoginRequest loginRequest = new LoginRequest("userTest", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new AuthenticationCredentialsNotFoundException("Bad credentials"));

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.login(loginRequest)
            );
            assertEquals("Bad credentials", exception.getMessage());
        }
    }

    @Nested
    class RefreshTest {

        @Test
        void refresh_validToken_shouldReturnAuthResponse() {
            RefreshRequest refreshRequest = new RefreshRequest("old-refresh-token");
            CustomUserDetails mockCustomUserDetails = mock(CustomUserDetails.class);

            when(mockCustomUserDetails.getUsername()).thenReturn("userTest");
            when(tokenBlacklistService.isTokenInBlacklist("old-refresh-token")).thenReturn(false);
            when(jwtService.isValidToken("old-refresh-token")).thenReturn(true);
            when(jwtService.refreshAccessToken("old-refresh-token", mockCustomUserDetails)).thenReturn("new-access-token");
            when(jwtService.generateRefreshToken(mockCustomUserDetails)).thenReturn("new-refresh-token");
            doNothing().when(tokenBlacklistService).addToBlacklist("old-refresh-token");

            AuthResponse response = authService.refresh(refreshRequest, mockCustomUserDetails);

            assertEquals("Bearer", response.tokenType());
            assertEquals("new-access-token", response.token());
            assertEquals("userTest", response.username());
            assertEquals("new-refresh-token", response.refreshToken());

            verify(tokenBlacklistService).addToBlacklist("old-refresh-token");
        }

        @Test
        void refresh_blacklistedToken_shouldThrowAuthenticationException() {
            RefreshRequest refreshRequest = new RefreshRequest("blacklisted-token");
            CustomUserDetails mockCustomUserDetails = mock(CustomUserDetails.class);

            when(tokenBlacklistService.isTokenInBlacklist("blacklisted-token")).thenReturn(true);

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.refresh(refreshRequest, mockCustomUserDetails
)
            );
            assertEquals("Refresh token is blacklisted", exception.getMessage());
        }

        @Test
        void refresh_invalidToken_shouldThrowAuthenticationException() {
            RefreshRequest refreshRequest = new RefreshRequest("invalid-token");
            CustomUserDetails mockCustomUserDetails = mock(CustomUserDetails.class);

            when(tokenBlacklistService.isTokenInBlacklist("invalid-token")).thenReturn(false);
            when(jwtService.isValidToken("invalid-token")).thenReturn(false);

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.refresh(refreshRequest, mockCustomUserDetails
)
            );
            assertEquals("Invalid or expired refresh token", exception.getMessage());
        }
    }

    @Nested
    class LogoutTest {

        @Test
        void logout_withAccessTokenOnly_shouldReturnApiMessage() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer access-token");
            when(mockRequest.getHeader("Refresh-Token")).thenReturn(null);
            when(jwtService.isValidToken("access-token")).thenReturn(true);
            doNothing().when(tokenBlacklistService).addToBlacklist("access-token");

            ApiMessageDto response = authService.logout(mockRequest);

            assertEquals("Logout successful", response.message());
            verify(tokenBlacklistService).addToBlacklist("access-token");
            verify(tokenBlacklistService, never()).addToBlacklist(argThat(token -> !"access-token".equals(token)));
        }

        @Test
        void logout_withBothTokens_shouldReturnApiMessage() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer access-token");
            when(mockRequest.getHeader("Refresh-Token")).thenReturn("refresh-token");
            when(jwtService.isValidToken("access-token")).thenReturn(true);
            when(jwtService.isValidToken("refresh-token")).thenReturn(true);
            doNothing().when(tokenBlacklistService).addToBlacklist(anyString());

            ApiMessageDto response = authService.logout(mockRequest);

            assertEquals("Logout successful", response.message());
            verify(tokenBlacklistService).addToBlacklist("access-token");
            verify(tokenBlacklistService).addToBlacklist("refresh-token");
        }

        @Test
        void  logout_noAuthorizationHeader_shouldThrowAuthenticationException() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.logout(mockRequest)
            );
            assertEquals("No Bearer token found in Authorization header", exception.getMessage());
        }

        @Test
        void logout_invalidAuthorizationHeaderFormat_shouldThrowAuthenticationException() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Invalid-header");

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.logout(mockRequest)
            );
            assertEquals("No Bearer token found in Authorization header", exception.getMessage());
        }

        @Test
        void logout_invalidAccessToken_shouldReturnApiMessageWithoutBlacklisting() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid-token");
            when(mockRequest.getHeader("Refresh-Token")).thenReturn(null);
            when(jwtService.isValidToken("invalid-token")).thenReturn(false);

            ApiMessageDto response = authService.logout(mockRequest);

            assertEquals("Logout successful", response.message());
            verify(tokenBlacklistService, never()).addToBlacklist("invalid-token");
        }

        @Test
        void logout_validAccessTokenAndInvalidRefreshToken_shouldBlacklistAccessOnly() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer access-token");
            when(mockRequest.getHeader("Refresh-Token")).thenReturn("invalid-refresh-token");
            when(jwtService.isValidToken("access-token")).thenReturn(true);
            when(jwtService.isValidToken("invalid-refresh-token")).thenReturn(false);
            doNothing().when(tokenBlacklistService).addToBlacklist("access-token");

            ApiMessageDto response = authService.logout(mockRequest);

            assertEquals("Logout successful", response.message());
            verify(tokenBlacklistService).addToBlacklist("access-token");
            verify(tokenBlacklistService, never()).addToBlacklist("invalid-refresh-token");
        }
    }
}