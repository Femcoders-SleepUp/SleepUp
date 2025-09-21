package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import com.SleepUp.SU.reservation.Reservation;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccommodationSpecificationTest {

    @Mock private Root<Accommodation> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder criteriaBuilder;
    @Mock private Predicate predicate;

    @Mock private Path<String> stringPath;
    @Mock private Path<Double> doublePath;
    @Mock private Path<Integer> intPath;
    @Mock private Path<LocalDate> datePath;

    private FilterAccommodationDTO filter;

    @BeforeEach
    void setUp() {
        filter = new FilterAccommodationDTO(
                "Hotel",
                "Luxury place",
                50.0,
                200.0,
                4,
                "Berlin",
                LocalDate.of(2025, 9, 15),
                LocalDate.of(2025, 9, 25));
    }

    private void stubStringProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) stringPath);
        when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
        lenient().when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
    }

    private void stubDateProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) datePath);
    }

    private void stubDoubleProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) doublePath);
    }

    private void stubNoBookingOverlap(LocalDate from, LocalDate to) {
        Subquery<Long> subquery = mock(Subquery.class);
        Root<Reservation> reservationRoot = mock(Root.class);
        Path<Object> resourcePath = mock(Path.class);
        Path<Object> resourceIdPath = mock(Path.class);
        Path<Object> rootIdPath = mock(Path.class);

        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Reservation.class)).thenReturn(reservationRoot);
        when(subquery.select(any())).thenReturn(subquery);

        when(reservationRoot.get("resource")).thenReturn(resourcePath);
        when(resourcePath.get("id")).thenReturn(resourceIdPath);
        when(root.get("id")).thenReturn(rootIdPath);

        when(criteriaBuilder.equal(resourceIdPath, rootIdPath)).thenReturn(predicate);

        when(reservationRoot.get("startDate")).thenReturn(mock(Path.class));
        when(reservationRoot.get("endDate")).thenReturn(mock(Path.class));

        if (to != null) {
            when(criteriaBuilder.greaterThan(any(), eq(to))).thenReturn(predicate);
        }
        if (from != null) {
            when(criteriaBuilder.lessThan(any(), eq(from))).thenReturn(predicate);
        }

        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(criteriaBuilder.not(any(Predicate.class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(subquery.where(any(Predicate.class))).thenReturn(subquery);
        when(criteriaBuilder.count(reservationRoot)).thenReturn(mock(Expression.class));
        when(criteriaBuilder.equal(subquery, 0L)).thenReturn(predicate);
    }

    @Test
    void testHasName() {
        stubStringProperty("name");
        Specification<Accommodation> spec = AccommodationSpecification.hasName(filter.name());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate instance returned from hasName");
        verify(criteriaBuilder).like(any(Expression.class), contains(filter.name().toLowerCase()));
    }

    @Test
    void testHasName_withNull() {
        Specification<Accommodation> spec = AccommodationSpecification.hasName(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for hasName with null input");
    }

    @Test
    void testHasDescription() {
        stubStringProperty("description");
        Specification<Accommodation> spec = AccommodationSpecification.hasDescription(filter.description());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate instance returned from hasDescription");
        verify(criteriaBuilder).like(any(Expression.class), contains(filter.description().toLowerCase()));
    }

    @Test
    void testHasDescription_withNull() {
        Specification<Accommodation> spec = AccommodationSpecification.hasDescription(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for hasDescription with null input");
    }

    @Test
    void testPriceBetween_bothNonNull() {
        stubDoubleProperty("price");
        when(criteriaBuilder.between(any(Expression.class), any(Double.class), any(Double.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(filter.minPrice(), filter.maxPrice());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for priceBetween with both bounds non-null");
        verify(criteriaBuilder).between(any(Expression.class), eq(filter.minPrice()), eq(filter.maxPrice()));
    }

    @Test
    void testPriceBetween_minNull() {
        stubDoubleProperty("price");
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), any(Double.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(null, filter.maxPrice());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for priceBetween with min null");
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq(filter.maxPrice()));
    }

    @Test
    void testPriceBetween_maxNull() {
        stubDoubleProperty("price");
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), any(Double.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(filter.minPrice(), null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for priceBetween with max null");
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq(filter.minPrice()));
    }

    @Test
    void testPriceBetween_bothNull() {
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(null, null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for priceBetween with both bounds null");
    }

    @Test
    void testGuestNumber() {
        @SuppressWarnings("unchecked")
        Path<Object> path = (Path<Object>) mock(Path.class);
        when(root.get("guestNumber")).thenReturn(path);
        Integer guestNumber = 2;
        when(criteriaBuilder.equal(path, guestNumber)).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.guestNumber(guestNumber);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for guestNumber");
    }

    @Test
    void testGuestNumber_withNull() {
        Specification<Accommodation> spec = AccommodationSpecification.guestNumber(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for guestNumber with null input");
    }

    @Test
    void testLocatedAt() {
        stubStringProperty("location");
        Specification<Accommodation> spec = AccommodationSpecification.locatedAt(filter.location());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for locatedAt");
        verify(criteriaBuilder).like(any(Expression.class), contains(filter.location().toLowerCase()));
    }

    @Test
    void testLocatedAt_withNull() {
        Specification<Accommodation> spec = AccommodationSpecification.locatedAt(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for locatedAt with null input");
    }

    @Test
    void testAvailableBetween_bothNonNull() {
        stubDateProperty("availableFrom");
        stubDateProperty("availableTo");
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(filter.fromDate(), filter.toDate());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for availableBetween with both dates non-null");
        verify(criteriaBuilder).and(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void testAvailableBetween_fromNull() {
        stubDateProperty("availableTo");
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(null, filter.toDate());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for availableBetween with from date null");
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq(filter.toDate()));
    }

    @Test
    void testAvailableBetween_toNull() {
        stubDateProperty("availableFrom");
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(filter.fromDate(), null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for availableBetween with to date null");
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq(filter.fromDate()));
    }

    @Test
    void testAvailableBetween_bothNull() {
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(null, null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for availableBetween with both dates null");
    }

    @Test
    void testNoBookingOverlap_withValidDates() {
        stubNoBookingOverlap(filter.fromDate(), filter.toDate());
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(filter.fromDate(), filter.toDate());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for noBookingOverlap with valid dates");
    }

    @Test
    void testNoBookingOverlap_withNullDates() {
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(null, null);
        Predicate result = spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
        assertNull(result, "Expected null predicate for noBookingOverlap with null dates");
    }

    @Test
    void testNoBookingOverlap_withNullStartDate() {
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(null, filter.toDate());
        Predicate result = spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
        assertNull(result, "Expected null predicate when newStartDate is null");
    }

    @Test
    void testNoBookingOverlap_withNullEndDate() {
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(filter.fromDate(), null);
        Predicate result = spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
        assertNull(result, "Expected null predicate when newEndDate is null");
    }

    @Test
    void testBuildSpecification() {
        stubStringProperty("name");
        stubStringProperty("description");
        stubDoubleProperty("price");
        when(root.get("guestNumber")).thenReturn((Path) intPath);
        stubStringProperty("location");
        stubDateProperty("availableFrom");
        stubDateProperty("availableTo");

        stubNoBookingOverlap(filter.fromDate(), filter.toDate());

        when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(criteriaBuilder.between(any(Expression.class), any(Double.class), any(Double.class))).thenReturn(predicate);
        when(criteriaBuilder.equal(any(Expression.class), any(Integer.class))).thenReturn(predicate);

        Specification<Accommodation> spec = new AccommodationSpecification().buildSpecification(filter);

        assertNotNull(spec, "Expected specification not null from buildSpecification");

        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertNotNull(result, "Expected non-null Predicate from buildSpecification toPredicate");
    }
}
