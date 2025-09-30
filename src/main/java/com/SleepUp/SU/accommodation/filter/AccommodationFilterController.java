package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Accommodation Filter", description = "Operations related to filtering accommodations")
@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
public class AccommodationFilterController {

    private final AccommodationFilterService accommodationFilterService;

    @GetMapping("/filter")
    @Operation(summary = "Filter Accommodations", description = "Retrieve a paginated list of accommodations based on filter criteria.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered accommodations"),
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
            })
    @ResponseStatus(HttpStatus.OK)
    public Page<AccommodationResponseSummary> getAllFilteredAccommodationsWithPagination(
            @ModelAttribute @Valid FilterAccommodationDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);
    }

}