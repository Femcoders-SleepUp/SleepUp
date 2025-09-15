package com.SleepUp.SU.user.user;

import com.SleepUp.SU.user.CustomUserDetails;
import com.SleepUp.SU.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserUserController {

    private final UserUserService userUserService;

    @GetMapping("/my-user")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getLoggedUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return userUserService.getLoggedUser(customUserDetails.getId());
    }
}
