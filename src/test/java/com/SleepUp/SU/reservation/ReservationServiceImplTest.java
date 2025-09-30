package com.SleepUp.SU.reservation;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.reservation.dto.*;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.exceptions.ReservationAccommodationOwnerException;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.reservationtime.ReservationTime;
import com.SleepUp.SU.reservation.service.ReservationServiceImpl;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.email.EmailServiceHelper;
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
public class ReservationServiceImplTest {

    @InjectMocks
    private ReservationServiceImpl reservationServiceImpl;

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
    class GetMyReservations {

        @Test
        void getMyReservations_allReservations_shouldReturnList() {
            when(reservationRepository.findByUser_Id(userId)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, ReservationTime.ALL);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_Id(userId);
        }

        @Test
        void getMyReservations_pastReservations_shouldReturnList() {
            LocalDate today = LocalDate.now();

            when(reservationRepository.findByUser_IdAndCheckInDateBefore(userId, today)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, ReservationTime.PAST);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_IdAndCheckInDateBefore(userId, today);
        }

        @Test
        void getMyReservations_futureReservations_shouldReturnList() {
            LocalDate today = LocalDate.now();

            when(reservationRepository.findByUser_IdAndCheckInDateAfter(userId, today)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, ReservationTime.FUTURE);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_IdAndCheckInDateAfter(userId, today);
        }

        @Test
        void getMyReservations_nullReservationTime_shouldReturnAll() {
            when(reservationRepository.findByUser_Id(userId)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, null);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_Id(userId);
        }
    }

    @Nested
    class CreateReservation {

        @Test
        void createReservation_validRequest_shouldReturnDetailResponse() {
            ReservationRequest reservationRequest = new ReservationRequest(2, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
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
                    reservationRequest.checkInDate(), reservationRequest.checkOutDate(),
                    BookingStatus.PENDING, false, LocalDateTime.now()
            );

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
            when(reservationMapper.toEntity(reservationRequest, BookingStatus.PENDING, user, accommodation, false)).thenReturn(mappedReservation);
            when(reservationRepository.save(mappedReservation)).thenReturn(savedReservation);
            when(reservationMapper.toDetail(savedReservation)).thenReturn(expectedResponse);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            doNothing().when(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationReservationOverlap(accommodationId, reservationRequest);
            doNothing().when(emailServiceHelper).sendOwnerReservedNotification(user, accommodation, savedReservation, 3);

            ReservationResponseDetail result = reservationServiceImpl.createReservation(reservationRequest, user, accommodationId);

            assertNotNull(result);
            assertEquals("Test Hotel", result.accommodationName());
            assertEquals("Test User", result.userName());
            assertEquals(BookingStatus.PENDING, result.bookingStatus());

            verify(reservationRepository).save(mappedReservation);
            verify(emailServiceHelper).sendOwnerReservedNotification(user, accommodation, savedReservation, 3);
        }

        @Test
        void createReservation_accommodationNotFound_shouldThrowException() {
            ReservationRequest reservationRequest = new ReservationRequest(2, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            User user = createTestUser();
            Long accommodationId = 999L;

            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenThrow(new AccommodationNotFoundByIdException(accommodationId));

            AccommodationNotFoundByIdException exception = assertThrows(AccommodationNotFoundByIdException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation with id '999' not found", exception.getMessage());
            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verify(accommodationServiceHelper).getAccommodationEntityById(accommodationId);
        }

        @Test
        void createReservation_invalidDates_shouldThrowException() {
            ReservationRequest reservationRequest = new ReservationRequest(2, LocalDate.now().plusDays(3), LocalDate.now().plusDays(1));
            User user = createTestUser();
            Long accommodationId = 1L;

            doThrow(new IllegalArgumentException("Check-in date must be before check-out date"))
                    .when(reservationServiceHelper).validateReservationDates(reservationRequest);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Check-in date must be before check-out date", exception.getMessage());
            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verifyNoInteractions(accommodationServiceHelper);
        }

        @Test
        void createReservation_accommodationUnavailable_shouldThrowException() {
            ReservationRequest reservationRequest = new ReservationRequest(5, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            User user = createTestUser();
            Long accommodationId = 1L;
            Accommodation accommodation = createTestAccommodation();

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doThrow(new IllegalArgumentException("Accommodation supports maximum 4 guests, but 5 guests requested"))
                    .when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation supports maximum 4 guests, but 5 guests requested", exception.getMessage());
        }

        @Test
        void createReservation_userHasOverlappingReservation_shouldThrowException() {
            ReservationRequest reservationRequest = new ReservationRequest(2, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            User user = createTestUser();
            Long accommodationId = 1L;
            Accommodation accommodation = createTestAccommodation();

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doNothing().when(reservationServiceHelper).validateAccommodationAvailability(accommodation, reservationRequest);
            doThrow(new IllegalArgumentException("You already have a reservation that overlaps with these dates"))
                    .when(reservationServiceHelper).validateUserReservationOverlap(user.getId(), reservationRequest);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, user, accommodationId));

            assertEquals("You already have a reservation that overlaps with these dates", exception.getMessage());
        }

        @Test
        void createReservation_userIsOwner_shouldThrowException() {
            ReservationRequest reservationRequest = new ReservationRequest(
                    2,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            User owner = createTestUser();
            Long accommodationId = 1L;

            Accommodation accommodation = createTestAccommodation();
            accommodation.setManagedBy(owner);

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId))
                    .thenReturn(accommodation);
            doNothing().when(reservationServiceHelper)
                    .validateReservationDates(reservationRequest);

            ReservationAccommodationOwnerException exception = assertThrows(
                    ReservationAccommodationOwnerException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, owner, accommodationId)
            );

            assertEquals("This accommodation is yours, you cannot book your own accommodations.",
                    exception.getMessage());
        }
    }

    @Nested
    class CancelReservation {

        @Test
        void cancelReservation_validReservation_shouldReturnCancelledDetail() {
            Long reservationId = 1L;
            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);

            Accommodation accommodation = new Accommodation();
            accommodation.setName("Test Accommodation");
            testReservation.setAccommodation(accommodation);

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(testReservation);
            when(reservationRepository.save(testReservation)).thenReturn(testReservation);

            ApiMessage result = reservationServiceImpl.cancelReservation(reservationId);

            assertNotNull(result);
            assertTrue(result.getMessage().contains("has been cancelled"));
            assertTrue(result.getMessage().contains("Test Accommodation"));
        }

        @Test
        void cancelReservation_reservationNotFound_shouldThrowException() {
            Long reservationId = 1L;

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenThrow(new RuntimeException("Reservation not found"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> reservationServiceImpl.cancelReservation(reservationId));

            assertEquals("Reservation not found", exception.getMessage());
        }

        @Test
        void cancelReservation_alreadyCancelled_shouldThrowException() {
            Long reservationId = 1L;

            Reservation testReservation = createTestReservation();
            testReservation.setId(reservationId);
            testReservation.setBookingStatus(BookingStatus.CANCELLED);

            when(reservationServiceHelper.getReservationEntityById(reservationId)).thenReturn(testReservation);
            doThrow(new IllegalStateException("Cannot modify a cancelled reservation"))
                    .when(reservationServiceHelper).validateReservationCancellable(testReservation);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> reservationServiceImpl.cancelReservation(reservationId));

            assertEquals("Cannot modify a cancelled reservation", exception.getMessage());
        }
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
        User owner = new User();
        owner.setId(2L);
        owner.setUsername("owner");
        owner.setEmail("owner@example.com");
        owner.setName("Accommodation Owner");
        owner.setRole(Role.USER);

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setName("Test Hotel");
        accommodation.setGuestNumber(4);
        accommodation.setAvailableFrom(LocalDate.now());
        accommodation.setAvailableTo(LocalDate.now().plusDays(30));
        accommodation.setManagedBy(owner);
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
