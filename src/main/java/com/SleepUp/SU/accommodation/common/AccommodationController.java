package com.SleepUp.SU.accommodation.common;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {
    private final AccommodationService accommodationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccommodationResponseSummary> getAllAccommodations(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationService.getAllAccommodations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccommodationResponseDetail createAccommodation(
            @RequestBody @Valid AccommodationRequest accommodationRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationService.createAccommodation(accommodationRequest, customUserDetails.getUser());
    }
}