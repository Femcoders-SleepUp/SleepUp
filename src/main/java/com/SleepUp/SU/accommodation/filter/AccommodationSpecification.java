package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.status.BookingStatus;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
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

    public static Specification<Accommodation> guestNumber(Integer guestNumber) {
        return (root, query, cb) ->
                guestNumber == null ? null : cb.equal(root.get("guestNumber"), guestNumber);
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

    public static Specification<Accommodation> noBookingOverlap(LocalDate newStartDate, LocalDate newEndDate) {
        return (root, query, cb) -> {
            if (newStartDate == null || newEndDate == null) {
                return null;
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Reservation> reservationRoot = subquery.from(Reservation.class);
            subquery.select(cb.count(reservationRoot));

            // Match reservations for the same accommodation
            Predicate sameAccommodation = cb.equal(reservationRoot.get("accommodation").get("id"), root.get("id"));

            // Exclude cancelled reservations
            Predicate notCancelled = cb.notEqual(reservationRoot.get("bookingStatus"), BookingStatus.CANCELLED);

            // Overlapping date ranges
            Predicate overlap = cb.not(cb.or(
                    cb.greaterThan(reservationRoot.get("checkInDate"), newEndDate),
                    cb.lessThan(reservationRoot.get("checkOutDate"), newStartDate)
            ));

            // Compose subquery where clause: same accommodation AND not cancelled AND overlapping
            subquery.where(cb.and(sameAccommodation, notCancelled, overlap));

            // Main predicate: count of overlapping, non-cancelled reservations must be zero
            return cb.equal(subquery, 0L);
        };
    }

    public Specification<Accommodation> buildSpecification(FilterAccommodationDTO filter) {
        return Specification.<Accommodation>unrestricted()
                .and(AccommodationSpecification.hasName(filter.name()))
                .and(AccommodationSpecification.hasDescription(filter.description()))
                .and(AccommodationSpecification.priceBetween(filter.minPrice(), filter.maxPrice()))
                .and(AccommodationSpecification.guestNumber(filter.guestNumber()))
                .and(AccommodationSpecification.locatedAt(filter.location()))
                .and(AccommodationSpecification.availableBetween(filter.fromDate(), filter.toDate()))
                .and(noBookingOverlap(filter.fromDate(), filter.toDate()));
    }
}
