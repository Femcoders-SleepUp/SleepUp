package com.SleepUp.SU.reservation.repository;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.status.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /**
     * Checks if a user has reservations that overlap with the dates provided
     * Cancelled reservations are excluded
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.user.id = :userId " +
            "AND r.bookingStatus != :cancelledStatus " +
            "AND (r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate)")
    boolean existsOverlappingReservationForUser(@Param("userId") Long userId,
                                                @Param("checkInDate") LocalDate checkInDate,
                                                @Param("checkOutDate") LocalDate checkOutDate,
                                                @Param("cancelledStatus") BookingStatus cancelledStatus);

    /**
     * Check if an accommodation has reservations that overlap with the dates provided
     * Cancelled reservations are excluded
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.accommodation.id = :accommodationId " +
            "AND r.bookingStatus != :cancelledStatus " +
            "AND (r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate)")
    boolean existsOverlappingReservationForAccommodation(@Param("accommodationId") Long accommodationId,
                                                         @Param("checkInDate") LocalDate checkInDate,
                                                         @Param("checkOutDate") LocalDate checkOutDate,
                                                         @Param("cancelledStatus") BookingStatus cancelledStatus);

    /**
     * Gets reservations that overlap with dates for a specific user
     * Useful for displaying conflict details
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId " +
            "AND r.bookingStatus != :cancelledStatus " +
            "AND (r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate)")
    List<Reservation> findOverlappingReservationsForUser(@Param("userId") Long userId,
                                                         @Param("checkInDate") LocalDate checkInDate,
                                                         @Param("checkOutDate") LocalDate checkOutDate,
                                                         @Param("cancelledStatus") BookingStatus cancelledStatus);

    /**
     * Gets reservations that overlap with dates for a specific accommodation
     * Useful for displaying conflict details
     */
    @Query("SELECT r FROM Reservation r WHERE r.accommodation.id = :accommodationId " +
            "AND r.bookingStatus != :cancelledStatus " +
            "AND (r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate)")
    List<Reservation> findOverlappingReservationsForAccommodation(@Param("accommodationId") Long accommodationId,
                                                                  @Param("checkInDate") LocalDate checkInDate,
                                                                  @Param("checkOutDate") LocalDate checkOutDate,
                                                                  @Param("cancelledStatus") BookingStatus cancelledStatus);

    /**
     * Checks if a user has made a reservation for a specific accommodation
     * in the last year (365 days) from the given date
     * Cancelled reservations are excluded
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.user.id = :userId " +
            "AND r.accommodation.id = :accommodationId " +
            "AND r.bookingStatus != :cancelledStatus " +
            "AND r.createdDate >= :oneYearAgo")
    boolean existsReservationLessThanYear(@Param("userId") Long userId,
                                          @Param("accommodationId") Long accommodationId,
                                          @Param("oneYearAgo") LocalDateTime oneYearAgo,
                                          @Param("cancelledStatus") BookingStatus cancelledStatus);

    List<Reservation> findByUser_Id(Long userId);

    List<Reservation> findByUser_IdAndCheckInDateBefore(Long userId, LocalDate date);

    List<Reservation> findByUser_IdAndCheckInDateAfter(Long userId, LocalDate date);

    List<Reservation> findByAccommodationId(Long id);

    List<Reservation> findByCheckInDate(LocalDate date);

    boolean existsByIdAndUser_Id(Long reservationId, Long userId);

}