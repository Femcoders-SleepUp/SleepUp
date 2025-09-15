package com.SleepUp.SU.auth;

import com.SleepUp.SU.auth.dto.AuthResponse;
import com.SleepUp.SU.auth.dto.LoginRequest;
import com.SleepUp.SU.auth.dto.RefreshRequest;
import com.SleepUp.SU.security.jwt.JwtService;
import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.ApiMessageDto;
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

    @Nested
    class RegisterNewUserTest {

        @Test
        void should_registerNewUser_fromRequest(){
            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");

            User mappedUser = new User();
            mappedUser.setUsername(userRequest.username());
            mappedUser.setEmail(userRequest.email());
            mappedUser.setName(userRequest.name());
            mappedUser.setPassword(userRequest.password());

            doNothing().when(userServiceHelper).validateUserDoesNotExist(userRequest.username(), userRequest.email());

            User userSaved = new User();
            userSaved.setId(1L);
            userSaved.setUsername("userTest");
            userSaved.setName("nameTest");
            userSaved.setEmail("usertest@test.com");
            userSaved.setPassword("password123");
            userSaved.setRole(Role.USER);

            when(userRepository.save(any(User.class))).thenReturn(userSaved);

            UserResponse userResponseMock = new UserResponse(
                    userSaved.getId(),
                    userSaved.getUsername(),
                    userSaved.getName(),
                    userSaved.getEmail(),
                    userSaved.getRole()
            );
            when(userMapper.toResponse(userSaved)).thenReturn(userResponseMock);
            when(passwordEncoder.encode(userRequest.password())).thenReturn("encondePassword");
            when(userMapper.toEntity(userRequest, "encondePassword", Role.USER)).thenReturn(mappedUser);

            UserResponse userResponse = authService.register(userRequest);

            assertEquals("userTest", userResponse.username());
            assertEquals("usertest@test.com", userResponse.email());
        }

        @Test
        void should_registerNewUser_throw_exceptionUsername(){
            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");

            doThrow(new RuntimeException("UsernameAlreadyExistException"))
                    .when(userServiceHelper).validateUserDoesNotExist(userRequest.username(), userRequest.email());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(userRequest));
            assertEquals("UsernameAlreadyExistException", exception.getMessage());
        }

        @Test
        void should_registerNewUser_throw_exceptionEmail(){
            UserRequest userRequest = new UserRequest("userTest2", "nameTest", "usertest@test.com", "password123");

            doThrow(new RuntimeException("EmailAlreadyExistException"))
                    .when(userServiceHelper).validateUserDoesNotExist(userRequest.username(), userRequest.email());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(userRequest));
            assertEquals("EmailAlreadyExistException", exception.getMessage());
        }

        @Test
        void should_RegisterNewUser_throw_dataIntegrityViolationException() {
            UserRequest userRequest = new UserRequest("userTest", "nameTest", "usertest@test.com", "password123");

            User mappedUser = new User();
            mappedUser.setUsername(userRequest.username());
            mappedUser.setEmail(userRequest.email());
            mappedUser.setName(userRequest.name());
            mappedUser.setPassword("encodedPassword");

            doNothing().when(userServiceHelper).validateUserDoesNotExist(userRequest.username(), userRequest.email());
            when(passwordEncoder.encode(userRequest.password())).thenReturn("encodedPassword");
            when(userMapper.toEntity(userRequest, "encodedPassword", Role.USER)).thenReturn(mappedUser);
            when(userRepository.save(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("Username or email already exists"));

            DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                    () -> authService.register(userRequest));
            assertEquals("Username or email already exists", exception.getMessage());
        }
    }

    @Nested
    class LoginTest {

        @Test
        void should_loginUser_successfully() {
            LoginRequest loginRequest = new LoginRequest("userTest", "password123");

            Authentication mockAuthentication = mock(Authentication.class);
            CustomUserDetails mockCustomUserDetails = mock(CustomUserDetails.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(mockAuthentication.getPrincipal()).thenReturn(mockCustomUserDetails);
//            when(mockCustomUserDetails.getUsername()).thenReturn("userTest");
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
        void should_loginUser_throw_authenticationException() {
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
        void should_refreshToken_successfully() {
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
        void should_refreshToken_throw_exception_when_token_is_blacklisted() {
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
        void should_refreshToken_throw_exception_when_token_is_invalid() {
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
        void should_logout_successfully_with_access_token_only() {
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
        void should_logout_successfully_with_both_tokens() {
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
        void should_logout_throw_exception_when_no_authorization_header() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.logout(mockRequest)
            );
            assertEquals("No Bearer token found in Authorization header", exception.getMessage());
        }

        @Test
        void should_logout_throw_exception_when_invalid_authorization_header() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Invalid-header");

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.logout(mockRequest)
            );
            assertEquals("No Bearer token found in Authorization header", exception.getMessage());
        }

        @Test
        void should_logout_successfully_with_invalid_access_token() {
            HttpServletRequest mockRequest = mock(HttpServletRequest.class);

            when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid-token");
            when(mockRequest.getHeader("Refresh-Token")).thenReturn(null);
            when(jwtService.isValidToken("invalid-token")).thenReturn(false);

            ApiMessageDto response = authService.logout(mockRequest);

            assertEquals("Logout successful", response.message());
            verify(tokenBlacklistService, never()).addToBlacklist("invalid-token");
        }

        @Test
        void should_logout_successfully_with_valid_access_token_and_invalid_refresh_token() {
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