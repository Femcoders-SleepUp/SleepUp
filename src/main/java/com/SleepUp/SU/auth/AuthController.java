package com.SleepUp.SU.auth;

import com.SleepUp.SU.user.UserService;
import com.SleepUp.SU.user.dto.USER.UserRequest;
import com.SleepUp.SU.user.dto.UserResponse;
import com.SleepUp.SU.utils.ApiMessageDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthServiceHelper authServiceHelper;

    @PostMapping("/logout")
    public ResponseEntity<ApiMessageDto> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        return userService.userLogout(request, response);
    }

}
