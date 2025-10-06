package com.SleepUp.SU.auth;


import com.SleepUp.SU.auth.dto.*;
import com.SleepUp.SU.security.jwt.JwtService;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.repository.UserRepository;
import com.SleepUp.SU.user.utils.UserServiceHelper;
import com.SleepUp.SU.user.dto.UserMapper;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserServiceHelper userServiceHelper;
    private final EmailService emailService;


    @Transactional
    public UserResponse register(UserRequest request) throws MessagingException {
        User savedUser = userServiceHelper.createUser(request, Role.USER);
        emailService.sendWelcomeEmail(savedUser);
        return userMapper.toResponse(savedUser);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse("login", "Bearer", accessToken, loginRequest.username(), refreshToken);
    }

    public AuthResponse refresh(RefreshRequest request, UserDetails userDetails) {
        if (tokenBlacklistService.isTokenInBlacklist(request.refreshToken())) {
            throw new AuthenticationCredentialsNotFoundException("Refresh token is blacklisted");
        }
        if (!jwtService.isValidToken(request.refreshToken())) {
            throw new AuthenticationCredentialsNotFoundException("Invalid or expired refresh token");
        }
        tokenBlacklistService.addToBlacklist(request.refreshToken());

        String newAccessToken = jwtService.refreshAccessToken(request.refreshToken(), userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse("refresh", "Bearer", newAccessToken, userDetails.getUsername(), newRefreshToken);
    }

    public ApiMessageDto logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationCredentialsNotFoundException("No Bearer token found in Authorization header");
        }

        String token = authorizationHeader.substring(7);
        if (jwtService.isValidToken(token)) {
            tokenBlacklistService.addToBlacklist(token);
        }

        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null && jwtService.isValidToken(refreshToken)) {
            tokenBlacklistService.addToBlacklist(refreshToken);
        }

        return new ApiMessageDto("Logout successful");
    }

}

