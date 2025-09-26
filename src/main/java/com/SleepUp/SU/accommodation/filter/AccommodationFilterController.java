package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.dto.AccommodationResponseSummary;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accommodations")
@RequiredArgsConstructor
public class AccommodationFilterController {

    private final AccommodationFilterService accommodationFilterService;

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<AccommodationResponseSummary> getAllFilteredAccommodationsWithPagination(
            @ModelAttribute @Valid FilterAccommodationDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return accommodationFilterService.getAllFilteredAccommodationsWithPagination(filter, pageable);
    }

}
