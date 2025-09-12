package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccommodationOwnerController {
    private final AccommodationOwnerService accommodationOwnerService;

    @GetMapping("/my-user")
    @ResponseStatus(HttpStatus.OK)
    public List<AccommodationResponseSummary> getAllOwnerAccommodations(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationOwnerService.getAllAccommodationsByOwnerId(customUserDetails.getId());
    }
}