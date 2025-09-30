package com.SleepUp.SU.accommodation.owner;

import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.user.entity.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Accommodation Owner", description = "Operations related to accommodations by owners")
@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccommodationOwnerController {
    private final AccommodationOwnerService accommodationOwnerService;

    @GetMapping("/me")
    @Operation(summary = "Get All My Accommodations", description = "Retrieve a list of accommodations owned by the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of accommodations"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public List<AccommodationResponseSummary> getAllOwnerAccommodations(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return accommodationOwnerService.getAllAccommodationsByOwnerId(customUserDetails.getId());
    }
}