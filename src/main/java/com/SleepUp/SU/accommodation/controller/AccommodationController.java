package com.SleepUp.SU.accommodation.controller;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.service.AccommodationServiceImpl;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {
    private final AccommodationServiceImpl accommodationServiceImpl;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccommodationResponseSummary> getAllAccommodations(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationServiceImpl.getAllAccommodations();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccommodationResponseDetail getAccommodationDetailById(@PathVariable Long id) {
        return accommodationServiceImpl.getAccommodationById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccommodationResponseDetail createAccommodation(
            @RequestBody @Valid @ModelAttribute AccommodationRequest accommodationRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationServiceImpl.createAccommodation(accommodationRequest, customUserDetails.getUser());
    }

    @PreAuthorize("hasRole('ADMIN') or @accommodationAccessEvaluator.isOwner(#id, principal.id)")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccommodationResponseDetail updateAccommodation(
            @PathVariable Long id,
            @RequestBody @Valid @ModelAttribute AccommodationRequest accommodationRequest){
        return accommodationServiceImpl.updateAccommodation(id, accommodationRequest);
    }

    @PreAuthorize("hasRole('ADMIN') or @accommodationAccessEvaluator.isOwner(#id, principal.id)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> deleteAccommodation(@PathVariable Long id){
        accommodationServiceImpl.deleteAccommodation(id);
        return ResponseEntity.noContent().build();
    }

}