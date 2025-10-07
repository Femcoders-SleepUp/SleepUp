package com.SleepUp.SU.accommodation.filter;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.dto.FilterAccommodationDTO;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.status.BookingStatus;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class AccommodationSpecificationTest {

    @Mock private Root<Accommodation> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder criteriaBuilder;
    @Mock private Predicate predicate;

    @Mock private Path<String> stringPath;
    @Mock private Path<Boolean> booleanPath;
    @Mock private Path<Double> doublePath;
    @Mock private Path<Integer> intPath;
    @Mock private Path<LocalDate> datePath;

    @Mock private Path<Object> accommodationPath;
    @Mock private Path<Object> accommodationIdPath;
    @Mock private Path<Object> bookingStatusPath;
    @Mock private Path<LocalDate> checkInDatePath;
    @Mock private Path<LocalDate> checkOutDatePath;

    private FilterAccommodationDTO filter;

    @BeforeEach
    void setUp() {
        filter = FilterAccommodationDTO.builder()
                .name("Hotel")
                .description("Luxury place")
                .minPrice(50.0)
                .maxPrice(200.0)
                .guestNumber(4)
                .location("Berlin")
                .fromDate(LocalDate.of(2025, 9, 15))
                .toDate(LocalDate.of(2025, 9, 25))
                .petFriendly(true)
                .build();
    }


    private void stubStringProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) stringPath);
        when(criteriaBuilder.lower(stringPath)).thenReturn(stringPath);
        lenient().when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(predicate);
    }


    private void stubBooleanProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) booleanPath);
        lenient().when(criteriaBuilder.isTrue(booleanPath)).thenReturn(predicate);
        lenient().when(criteriaBuilder.isFalse( booleanPath)).thenReturn(predicate);
    }


    private void stubDateProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) datePath);
    }


    private void stubDoubleProperty(String propertyName) {
        when(root.get(propertyName)).thenReturn((Path) doublePath);
    }

    private void stubReservationPaths(Subquery<Long> subquery,
                                      Root<Reservation> reservationRoot,
                                      LocalDate newStartDate,
                                      LocalDate newEndDate) {
        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(Reservation.class)).thenReturn(reservationRoot);
        when(subquery.select(any())).thenReturn(subquery);

        when(reservationRoot.get("accommodation")).thenReturn(accommodationPath);
        when(accommodationPath.get("id")).thenReturn(accommodationIdPath);
        when(reservationRoot.get("bookingStatus")).thenReturn(bookingStatusPath);
        when(reservationRoot.<LocalDate>get("checkInDate")).thenReturn(checkInDatePath);
        when(reservationRoot.<LocalDate>get("checkOutDate")).thenReturn(checkOutDatePath);

        when(criteriaBuilder.equal(eq(accommodationIdPath), any())).thenReturn(predicate);
        when(criteriaBuilder.notEqual(bookingStatusPath, BookingStatus.CANCELLED)).thenReturn(predicate);
        when(criteriaBuilder.greaterThan(checkInDatePath, newEndDate)).thenReturn(predicate);
        when(criteriaBuilder.lessThan(checkOutDatePath, newStartDate)).thenReturn(predicate);

        when(criteriaBuilder.or(predicate, predicate)).thenReturn(predicate);
        when(criteriaBuilder.not(predicate)).thenReturn(predicate);
        when(criteriaBuilder.and(predicate, predicate)).thenReturn(predicate);
        when(subquery.where(predicate)).thenReturn(subquery);
        when(criteriaBuilder.equal(subquery, 0L)).thenReturn(predicate);
    }

    @Test
    void hasName_validInput_shouldReturnLikePredicate() {
        stubStringProperty("name");
        Specification<Accommodation> spec = AccommodationSpecification.hasName(filter.name());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate instance returned from hasName");
        verify(criteriaBuilder).like(any(Expression.class), contains(filter.name().toLowerCase()));
    }

    @Test
    void  hasName_nullInput_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.hasName(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for hasName with null input");
    }

    @Test
    void hasDescription_validInput_shouldReturnLikePredicate() {
        stubStringProperty("description");
        Specification<Accommodation> spec = AccommodationSpecification.hasDescription(filter.description());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate instance returned from hasDescription");
        verify(criteriaBuilder).like(any(Expression.class), contains(filter.description().toLowerCase()));
    }

    @Test
    void hasDescription_nullInput_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.hasDescription(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for hasDescription with null input");
    }

    @Test
    void priceBetween_bothBoundsNonNull_shouldReturnBetweenPredicate() {
        stubDoubleProperty("price");
        when(criteriaBuilder.between(any(Expression.class), any(Double.class), any(Double.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(filter.minPrice(), filter.maxPrice());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for priceBetween with both bounds non-null");
        verify(criteriaBuilder).between(any(Expression.class), eq(filter.minPrice()), eq(filter.maxPrice()));
    }

    @Test
    void priceBetween_minNull_shouldReturnLessThanOrEqualToPredicate() {
        stubDoubleProperty("price");
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), any(Double.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(null, filter.maxPrice());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for priceBetween with min null");
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq(filter.maxPrice()));
    }

    @Test
    void  priceBetween_maxNull_shouldReturnGreaterThanOrEqualToPredicate() {
        stubDoubleProperty("price");
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), any(Double.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(filter.minPrice(), null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for priceBetween with max null");
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq(filter.minPrice()));
    }

    @Test
    void priceBetween_bothNull_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.priceBetween(null, null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for priceBetween with both bounds null");
    }

    @Test
    void guestNumber_validInput_shouldReturnEqualPredicate() {
        Path<Object> path = (Path<Object>) mock(Path.class);
        when(root.get("guestNumber")).thenReturn(path);
        Integer guestNumber = 2;
        when(criteriaBuilder.equal(path, guestNumber)).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.guestNumber(guestNumber);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for guestNumber");
    }

    @Test
    void guestNumber_nullInput_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.guestNumber(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for guestNumber with null input");
    }

    @Test
    void locatedAt_validInput_shouldReturnLikePredicate() {
        stubStringProperty("location");
        Specification<Accommodation> spec = AccommodationSpecification.locatedAt(filter.location());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for locatedAt");
        verify(criteriaBuilder).like(any(Expression.class), contains(filter.location().toLowerCase()));
    }

    @Test
    void locatedAt_nullInput_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.locatedAt(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for locatedAt with null input");
    }

    @Test
    void availableBetween_bothDatesNonNull_shouldReturnAndPredicate() {
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
    void availableBetween_fromDateNull_shouldReturnLessThanOrEqualToPredicate() {
        stubDateProperty("availableTo");
        when(criteriaBuilder.lessThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(null, filter.toDate());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for availableBetween with from date null");
        verify(criteriaBuilder).lessThanOrEqualTo(any(Expression.class), eq(filter.toDate()));
    }

    @Test
    void availableBetween_toDateNull_shouldReturnGreaterThanOrEqualToPredicate() {
        stubDateProperty("availableFrom");
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(filter.fromDate(), null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate for availableBetween with to date null");
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq(filter.fromDate()));
    }

    @Test
    void availableBetween_bothDatesNull_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.availableBetween(null, null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for availableBetween with both dates null");
    }

    @Test
    void noBookingOverlap_validDates_shouldReturnNonBookingOverlapPredicate() {
        LocalDate newStartDate = filter.fromDate();
        LocalDate newEndDate = filter.toDate();

        Subquery<Long> subquery = mock(Subquery.class);
        Root<Reservation> reservationRoot = mock(Root.class);
        stubReservationPaths(subquery, reservationRoot, newStartDate, newEndDate);
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(newStartDate, newEndDate);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result, "Expected non-null predicate for valid date range");
        assertNotNull(query, "Query should not be null during predicate creation");
    }

    @Test
    void noBookingOverlap_nullDates_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(null, null);
        Predicate result = spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
        assertNull(result, "Expected null predicate for noBookingOverlap with null dates");
    }

    @Test
    void noBookingOverlap_nullStartDate_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(null, filter.toDate());
        Predicate result = spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
        assertNull(result, "Expected null predicate when newStartDate is null");
    }

    @Test
    void noBookingOverlap_nullEndDate_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.noBookingOverlap(filter.fromDate(), null);
        Predicate result = spec.toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
        assertNull(result, "Expected null predicate when newEndDate is null");
    }

    @Test
    void petFriendly_trueInput_shouldReturnIsTruePredicate() {
        stubBooleanProperty("petFriendly");
        Specification<Accommodation> spec = AccommodationSpecification.petFriendly(true);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate instance returned from petFriendly(true)");
        verify(criteriaBuilder).isTrue(any(Expression.class));
    }

    @Test
    void petFriendly_falseInput_shouldReturnIsFalsePredicate() {
        stubBooleanProperty("petFriendly");
        Specification<Accommodation> spec = AccommodationSpecification.petFriendly(false);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        assertSame(predicate, result, "Expected same predicate instance returned from petFriendly(false)");
        verify(criteriaBuilder).isFalse(any(Expression.class));
    }

    @Test
    void petFriendly_nullInput_shouldReturnNull() {
        Specification<Accommodation> spec = AccommodationSpecification.petFriendly(null);
        assertNull(spec.toPredicate(root, query, criteriaBuilder), "Expected null predicate for petFriendly(null)");
    }

    @Test
    void buildSpecification_validFilter_shouldReturnCombinedPredicate() {
        LocalDate newStartDate = filter.fromDate();
        LocalDate newEndDate = filter.toDate();

        stubStringProperty("name");
        stubStringProperty("description");
        stubDoubleProperty("price");
        when(root.get("guestNumber")).thenReturn((Path) intPath);
        stubStringProperty("location");
        stubDateProperty("availableFrom");
        stubDateProperty("availableTo");
        stubBooleanProperty("petFriendly");

        Subquery<Long> subquery = mock(Subquery.class);
        Root<Reservation> reservationRoot = mock(Root.class);
        stubReservationPaths(subquery, reservationRoot, newStartDate, newEndDate);

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
