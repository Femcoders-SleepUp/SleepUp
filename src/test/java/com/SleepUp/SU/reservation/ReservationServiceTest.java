package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.AccommodationRepository;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.UserRepository;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.EmailServiceHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private EmailServiceHelper emailServiceHelper;

    @Mock
    private UserRepository userRepository;

    @Nested
    class CreateReservationTest {

        @Test
        void should_createReservation_successfully() {
            // Given
            ReservationRequest reservationRequest = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            User user = createTestUser();
            Long accommodationId = 1L;
            Accommodation accommodation = createTestAccommodation();

            Reservation mappedReservation = createTestReservation();
            Reservation savedReservation = createTestReservation();
            savedReservation.setId(1L);
            savedReservation.setUser(user);
            savedReservation.setAccommodation(accommodation);
            savedReservation.setCreatedDate(LocalDateTime.now());

            ReservationResponseDetail expectedResponse = new ReservationResponseDetail(
                    1L, "Test User", 2, "Test Hotel",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                    BookingStatus.PENDING, false, LocalDateTime.now()
            );

            // When
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
            when(reservationMapper.toEntity(reservationRequest, BookingStatus.PENDING, user, accommodation, false))
                    .thenReturn(mappedReservation);
            when(reservationRepository.save(mappedReservation)).thenReturn(savedReservation);
            when(reservationMapper.toDetail(savedReservation)).thenReturn(expectedResponse);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            doNothing().when(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationReservationOverlap(accommodationId, reservationRequest);
            doNothing().when(emailServiceHelper).sendOwnerReservedNotification(user, accommodation, savedReservation);

            // Then
            ReservationResponseDetail result = reservationService.createReservation(reservationRequest, user, accommodationId);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals(BookingStatus.PENDING, result.bookingStatus());
            assertEquals("Test Hotel", result.accommodationName());
            assertEquals("Test User", result.userName());
            assertEquals(2, result.guestNumber());

            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verify(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            verify(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);
            verify(reservationServiceHelper).validateAccommodationReservationOverlap(accommodationId, reservationRequest);
            verify(accommodationRepository).findById(accommodationId);
            verify(reservationRepository).save(mappedReservation);
            verify(emailServiceHelper).sendOwnerReservedNotification(user, accommodation, savedReservation);
        }

        @Test
        void should_createReservation_throw_exception_when_accommodation_not_found() {
            // Given
            ReservationRequest reservationRequest = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );
            User user = createTestUser();
            Long accommodationId = 999L;

            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation not found", exception.getMessage());
            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verify(accommodationRepository).findById(accommodationId);
            verifyNoInteractions(reservationRepository);
            verifyNoInteractions(emailServiceHelper);
        }

        @Test
        void should_createReservation_throw_exception_when_dates_invalid() {
            // Given
            ReservationRequest reservationRequest = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(3),
                    LocalDate.now().plusDays(1)
            );
            User user = createTestUser();
            Long accommodationId = 1L;

            doThrow(new IllegalArgumentException("Check-in date must be before check-out date"))
                    .when(reservationServiceHelper).validateReservationDates(reservationRequest);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Check-in date must be before check-out date", exception.getMessage());
            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verifyNoInteractions(accommodationRepository);
            verifyNoInteractions(reservationRepository);
        }

        @Test
        void should_createReservation_throw_exception_when_accommodation_unavailable() {
            // Given
            ReservationRequest reservationRequest = new ReservationRequest(
                    5,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );
            User user = createTestUser();
            Long accommodationId = 1L;
            Accommodation accommodation = createTestAccommodation();

            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doThrow(new IllegalArgumentException("Accommodation supports maximum 4 guests, but 5 guests requested"))
                    .when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation supports maximum 4 guests, but 5 guests requested", exception.getMessage());
            verify(accommodationRepository).findById(accommodationId);
            verify(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            verifyNoInteractions(reservationRepository);
        }

        @Test
        void should_createReservation_throw_exception_when_user_has_overlapping_reservation() {
            // Given
            ReservationRequest reservationRequest = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );
            User user = createTestUser();
            Long accommodationId = 1L;
            Accommodation accommodation = createTestAccommodation();

            when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            doThrow(new IllegalArgumentException("You already have a reservation that overlaps with these dates"))
                    .when(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("You already have a reservation that overlaps with these dates", exception.getMessage());
            verify(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);
            verifyNoInteractions(reservationRepository);
        }

        private User createTestUser() {
            User user = new User();
            user.setId(1L);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setName("Test User");
            user.setRole(Role.USER);
            return user;
        }

        private Accommodation createTestAccommodation() {
            Accommodation accommodation = new Accommodation();
            accommodation.setId(1L);
            accommodation.setName("Test Hotel");
            accommodation.setGuestNumber(4);
            accommodation.setAvailableFrom(LocalDate.now());
            accommodation.setAvailableTo(LocalDate.now().plusDays(30));
            return accommodation;
        }

        private Reservation createTestReservation() {
            Reservation reservation = new Reservation();
            reservation.setCheckInDate(LocalDate.now().plusDays(1));
            reservation.setCheckOutDate(LocalDate.now().plusDays(3));
            reservation.setGuestNumber(2);
            reservation.setBookingStatus(BookingStatus.PENDING);
            reservation.setEmailSent(false);
            return reservation;
        }

        @Test
        void getMyReservations_returnsList() {
            List<ReservationResponseSummary> listResult = runMyReservationsTest("María");
            assertEquals(1, listResult.size());
            assertEquals("María", listResult.get(0).userName());
            assertEquals(1L, listResult.get(0).id());
        }

        private List<ReservationResponseSummary> runMyReservationsTest(String userName) {
            User user = new User();
            user.setId(1L);
            user.setUsername(userName);

            Reservation reservation = new Reservation();
            reservation.setId(1L);

            ReservationResponseSummary summary = new ReservationResponseSummary(1L,userName,1,"María House",null,null,null,null,null);
            when(userRepository.findByUsername(userName)).thenReturn(Optional.of(user));
            when(reservationRepository.findByUser(user)).thenReturn(List.of(reservation));
            when(reservationMapper.toSummary(reservation)).thenReturn(summary);

            return reservationService.getMyReservations(userName);
        }
    }
}