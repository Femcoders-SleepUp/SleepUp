package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.reservation.dto.ReservationMapper;
import com.SleepUp.SU.reservation.dto.ReservationRequest;
import com.SleepUp.SU.reservation.dto.ReservationResponseDetail;
import com.SleepUp.SU.reservation.dto.ReservationResponseSummary;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.User;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.EmailServiceHelper;
import com.SleepUp.SU.utils.EntityUtil;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private EntityUtil entityUtil;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationServiceHelper reservationServiceHelper;

    @Mock
    private AccommodationServiceHelper accommodationServiceHelper;

    @Mock
    private EmailServiceHelper emailServiceHelper;

    private ReservationResponseSummary mappedDtos;
    private Long userId;
    private List<Reservation> mockReservations;

    @BeforeEach
    void setUp(){
        mappedDtos = new ReservationResponseSummary(1L, "Maria",1,"María House",null,null,null,null,null);
        userId = 1L;
        mockReservations = List.of(new Reservation());

    }

    @Nested
    class GetReservationsTest {
        @Test
        public void testGetMyReservations_All() {
            when(reservationRepository.findByUser_Id(userId)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationService.getMyReservations(userId, ReservationTime.ALL);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_Id(userId);
        }

        @Test
        public void testGetMyReservations_Past() {
            LocalDate today = LocalDate.now();

            when(reservationRepository.findByUser_IdAndCheckInDateBefore(userId, today)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationService.getMyReservations(userId, ReservationTime.PAST);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_IdAndCheckInDateBefore(userId, today);
        }

        @Test
        public void testGetMyReservations_Future() {
            LocalDate today = LocalDate.now();

            when(reservationRepository.findByUser_IdAndCheckInDateAfter(userId, today)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationService.getMyReservations(userId, ReservationTime.FUTURE);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_IdAndCheckInDateAfter(userId, today);
        }

        @Test
        public void testGetMyReservations_NullTime() {
            when(reservationRepository.findByUser_Id(userId)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationService.getMyReservations(userId, null);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_Id(userId);
        }

    }

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
            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
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
            verify(accommodationServiceHelper).getAccommodationEntityById(accommodationId);
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
            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenThrow(new AccommodationNotFoundByIdException(accommodationId));

            // When & Then
            AccommodationNotFoundByIdException exception = assertThrows(AccommodationNotFoundByIdException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation with id '999' not found", exception.getMessage());
            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verify(accommodationServiceHelper).getAccommodationEntityById(accommodationId);
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
            verifyNoInteractions(accommodationServiceHelper);
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

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doThrow(new IllegalArgumentException("Accommodation supports maximum 4 guests, but 5 guests requested"))
                    .when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation supports maximum 4 guests, but 5 guests requested", exception.getMessage());
            verify(accommodationServiceHelper).getAccommodationEntityById(accommodationId);
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

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            doThrow(new IllegalArgumentException("You already have a reservation that overlaps with these dates"))
                    .when(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(reservationRequest, user, accommodationId));

            assertEquals("You already have a reservation that overlaps with these dates", exception.getMessage());
            verify(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);
            verify(accommodationServiceHelper).getAccommodationEntityById(accommodationId);
        }

        @Test
        void should_cancelReservation_successfully() {
            Long reservationId = 1L;
            Long userId = 1L;

            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);

            ReservationResponseDetail expectedResponse = expectedCancelledResponse();

            when(reservationServiceHelper.getReservationEntityById(reservationId))
                    .thenReturn(testReservation);
            when(reservationRepository.save(testReservation)).thenReturn(testReservation);
            when(reservationMapper.toDetail(testReservation)).thenReturn(expectedCancelledResponse());

            ReservationResponseDetail result = reservationService.cancelReservation(reservationId);

            assertNotNull(result);
            assertEquals(reservationId, result.id());
            assertEquals(BookingStatus.CANCELLED, result.bookingStatus());
            verify(reservationServiceHelper).getReservationEntityById(reservationId);
            verify(reservationRepository).save(testReservation);
            verify(reservationMapper).toDetail(testReservation);
        }

        @Test
        void should_cancelReservation_throw_exception_when_not_found() {
            Long reservationId = 1L;
            Long userId = 1L;

            when(reservationServiceHelper.getReservationEntityById(reservationId))
                    .thenThrow(new RuntimeException("Reservation not found"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> reservationService.cancelReservation(reservationId));

            assertEquals("Reservation not found", exception.getMessage());
            verify(reservationServiceHelper).getReservationEntityById(reservationId);
            verify(reservationRepository, never()).save(any());
        }

        @Test
        void should_cancelReservation_throw_exception_when_already_cancelled() {
            Long reservationId = 1L;
            Long userId = 1L;

            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);
            testReservation.setBookingStatus(BookingStatus.CANCELLED);

            when(reservationServiceHelper.getReservationEntityById(reservationId))
                    .thenReturn(testReservation);
            doThrow(new IllegalStateException("Cannot modify a cancelled reservation"))
                    .when(reservationServiceHelper).validateReservationCancellable(testReservation);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> reservationService.cancelReservation(reservationId));

            assertEquals("Cannot modify a cancelled reservation", exception.getMessage());
            verify(reservationServiceHelper).getReservationEntityById(reservationId);
            verify(reservationServiceHelper).validateReservationCancellable(testReservation);
            verify(reservationRepository, never()).save(any());
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

        private ReservationResponseDetail expectedCancelledResponse() {
            return new ReservationResponseDetail(
                    1L, "Test User", 4, "Test Hotel",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                    BookingStatus.CANCELLED, false, LocalDateTime.now()
            );
        }

    }
}