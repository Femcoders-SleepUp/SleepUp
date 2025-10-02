package com.SleepUp.SU.auth;

import com.SleepUp.SU.auth.dto.*;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import com.SleepUp.SU.user.dto.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.dto.ApiMessageDto;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody UserRequest request) throws MessagingException {
        return authService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request,
                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return authService.refresh(request, customUserDetails);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiMessageDto logout(HttpServletRequest request) {
        return authService.logout(request);
    }
}
