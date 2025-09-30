package com.SleepUp.SU.accommodation.controller;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseDetail;
import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.service.AccommodationService;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accommodation", description = "Operation related to accommodations")
@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
public class AccommodationController {
    private final AccommodationService accommodationService;

    @GetMapping
    @Operation(summary = "Get All Accommodations", description = "Retrieve a list of all accommodations with summary details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of accommodations"),
                    @ApiResponse(responseCode = "204", ref = "#/components/responses/NoContent"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public List<AccommodationResponseSummary> getAllAccommodations(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationService.getAllAccommodations();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Accommodation Details", description = "Retrieve detailed information about a specific accommodation by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved accommodation details"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/AccommodationFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public AccommodationResponseDetail getAccommodationDetailById(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Accommodation", description = "Create a new accommodation with the provided details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully created a new accommodation"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.CREATED)
    public AccommodationResponseDetail createAccommodation(
            @Valid @ModelAttribute AccommodationRequest accommodationRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationService.createAccommodation(accommodationRequest, customUserDetails.getUser());
    }

    @PreAuthorize("hasRole('ADMIN') or @accommodationAccessEvaluator.isOwner(#id, principal.id)")
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Accommodation", description = "Update the details of an existing accommodation by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated accommodation details"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/AccommodationFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public AccommodationResponseDetail updateAccommodation(
            @PathVariable Long id,
            @Valid @ModelAttribute AccommodationRequest accommodationRequest){
        return accommodationService.updateAccommodation(id, accommodationRequest);
    }

    @PreAuthorize("hasRole('ADMIN') or @accommodationAccessEvaluator.isOwner(#id, principal.id)")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Accommodation", description = "Delete an existing accommodation by its ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted the accommodation"),
                    @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/AccommodationFound"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> deleteAccommodation(@PathVariable Long id){
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.noContent().build();
    }

}