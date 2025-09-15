package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import org.springframework.data.jpa.domain.Specification;
import com.SleepUp.SU.accommodation.Accommodation;

import java.time.LocalDate;

public class AccommodationSpecification {

    public static Specification<Accommodation> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Accommodation> hasDescription(String description) {
        return (root, query, cb) ->
                description == null ? null : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Accommodation> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice == null) return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            if (maxPrice == null) return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }

    public static Specification<Accommodation> guestNumber(int guestNumber) {
        return (root, query, cb) -> cb.equal(root.get("guestNumber"), guestNumber);
    }

    public static Specification<Accommodation> locatedAt(String location) {
        return (root, query, cb) ->
                location == null ? null : cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Accommodation> availableBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from == null) return cb.lessThanOrEqualTo(root.get("availableTo"), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("availableFrom"), from);
            return cb.and(
                    cb.lessThanOrEqualTo(root.get("availableFrom"), to),
                    cb.greaterThanOrEqualTo(root.get("availableTo"), from)
            );
        };
    }

    public Specification<Accommodation> buildSpecification(FilterAccommodationDTO filter) {
        return Specification.<Accommodation>unrestricted()
                .and(AccommodationSpecification.hasName(filter.name()))
                .and(AccommodationSpecification.hasDescription(filter.description()))
                .and(AccommodationSpecification.priceBetween(filter.minPrice(), filter.maxPrice()))
                .and(AccommodationSpecification.guestNumber(filter.guestNumber()))
                .and(AccommodationSpecification.locatedAt(filter.location()))
                .and(AccommodationSpecification.availableBetween(filter.fromDate(), filter.toDate()));
    }
}
