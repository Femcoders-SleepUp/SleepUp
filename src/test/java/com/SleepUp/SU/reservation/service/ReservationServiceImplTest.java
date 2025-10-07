package com.SleepUp.SU.reservation.service;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.exceptions.InvalidDateRangeError;
import com.SleepUp.SU.exceptions.InvalidDateRangeException;
import com.SleepUp.SU.reservation.dto.*;
import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.reservation.repository.ReservationRepository;
import com.SleepUp.SU.reservation.status.BookingStatus;
import com.SleepUp.SU.reservation.utils.ReservationServiceHelper;
import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.role.Role;
import com.SleepUp.SU.utils.email.EmailService;
import com.SleepUp.SU.utils.EntityUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private EmailService emailService;

    private ReservationResponseSummary mappedDtos;
    private Long userId;
    private List<Reservation> mockReservations;
    private User user;
    private Accommodation accommodation;
    private Reservation reservation;
    private Reservation mappedReservation;
    private Reservation savedReservation;

    @BeforeEach
    void setUp() {
        mappedDtos = new ReservationResponseSummary(1L, "Maria", 1, "María House", null, null, null, null);
        userId = 1L;
        mockReservations = List.of(new Reservation());

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .role(Role.USER)
                .build();

        User owner = User.builder()
                .id(2L)
                .username("owner")
                .email("owner@example.com")
                .name("Accommodation Owner")
                .role(Role.USER)
                .build();

        accommodation = Accommodation.builder()
                .id(1L)
                .name("Test Hotel")
                .guestNumber(4)
                .availableFrom(LocalDate.now())
                .availableTo(LocalDate.now().plusDays(30))
                .price(10.0)
                .managedBy(owner)
                .build();

        reservation = Reservation.builder()
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .guestNumber(2)
                .bookingStatus(BookingStatus.PENDING)
                .accommodation(accommodation)
                .user(user)
                .emailSent(false)
                .createdDate(LocalDateTime.now())
                .build();

        mappedReservation = Reservation.builder()
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestNumber(reservation.getGuestNumber())
                .bookingStatus(BookingStatus.PENDING)
                .accommodation(accommodation)
                .user(user)
                .emailSent(false)
                .build();

        savedReservation = Reservation.builder()
                .id(1L)
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .guestNumber(reservation.getGuestNumber())
                .bookingStatus(BookingStatus.PENDING)
                .accommodation(accommodation)
                .user(user)
                .emailSent(false)
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Nested
    class GetMyReservations {

        @Test
        void getMyReservations_allReservations_shouldReturnList() {
            when(reservationRepository.findByUser_Id(userId)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, com.SleepUp.SU.reservation.reservationTime.ReservationTime.ALL);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_Id(userId);
        }

        @Test
        void getMyReservations_pastReservations_shouldReturnList() {
            LocalDate today = LocalDate.now();

            when(reservationRepository.findByUser_IdAndCheckInDateBefore(userId, today)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, com.SleepUp.SU.reservation.reservationTime.ReservationTime.PAST);

            assertEquals(List.of(mappedDtos), result);
            verify(reservationRepository).findByUser_IdAndCheckInDateBefore(userId, today);
        }

        @Test
        void getMyReservations_futureReservations_shouldReturnList() {
            LocalDate today = LocalDate.now();

            when(reservationRepository.findByUser_IdAndCheckInDateAfter(userId, today)).thenReturn(mockReservations);
            when(entityUtil.mapEntitiesToDTOs(eq(mockReservations), any())).thenReturn(List.of(mappedDtos));

            List<ReservationResponseSummary> result = reservationServiceImpl.getMyReservations(userId, com.SleepUp.SU.reservation.reservationTime.ReservationTime.FUTURE);

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
            Long accommodationId = 1L;

            ReservationResponseDetail expectedResponse = new ReservationResponseDetail(
                    1L,
                    "Test User",
                    2,
                    "Test Hotel",
                    reservationRequest.checkInDate(),
                    reservationRequest.checkOutDate(),
                    BookingStatus.PENDING,
                    false,
                    savedReservation.getCreatedDate(),
                    BigDecimal.valueOf(100)
            );

            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId)).thenReturn(accommodation);
            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            doNothing().when(reservationServiceHelper).validateCreateReservation(accommodation, user, reservationRequest);
            when(reservationMapper.toEntity(reservationRequest, BookingStatus.PENDING, user, accommodation, false)).thenReturn(mappedReservation);
            doNothing().when(reservationServiceHelper).updatePriceWithDiscountIfDeserved(mappedReservation, accommodation, user);
            when(reservationRepository.save(mappedReservation)).thenReturn(savedReservation);
            when(reservationMapper.toDetail(savedReservation)).thenReturn(expectedResponse);
            doNothing().when(emailService).sendOwnerReservedNotification(savedReservation);

            ReservationResponseDetail result = reservationServiceImpl.createReservation(reservationRequest, user, accommodationId);

            assertNotNull(result);
            assertEquals(expectedResponse.accommodationName(), result.accommodationName());
            assertEquals(expectedResponse.userName(), result.userName());
            assertEquals(expectedResponse.bookingStatus(), result.bookingStatus());

            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verify(reservationServiceHelper).validateCreateReservation(accommodation, user, reservationRequest);
            verify(reservationServiceHelper).updatePriceWithDiscountIfDeserved(mappedReservation, accommodation, user);
            verify(reservationRepository).save(mappedReservation);
            verify(emailService).sendOwnerReservedNotification(savedReservation);
        }

        @Test
        void createReservation_accommodationNotFound_shouldThrow() {
            ReservationRequest reservationRequest = new ReservationRequest(2, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            Long accommodationId = 999L;

            doNothing().when(reservationServiceHelper).validateReservationDates(reservationRequest);
            when(accommodationServiceHelper.getAccommodationEntityById(accommodationId))
                    .thenThrow(new AccommodationNotFoundByIdException(accommodationId));

            AccommodationNotFoundByIdException exception = assertThrows(AccommodationNotFoundByIdException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, user, accommodationId));

            assertEquals("Accommodation with id '999' not found", exception.getMessage());

            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verify(accommodationServiceHelper).getAccommodationEntityById(accommodationId);
        }

        @Test
        void createReservation_invalidDates_shouldThrow() {
            ReservationRequest reservationRequest = new ReservationRequest(2, LocalDate.now().plusDays(3), LocalDate.now().plusDays(1));
            Long accommodationId = 1L;

            doThrow(new InvalidDateRangeException(InvalidDateRangeError.ORDER))
                    .when(reservationServiceHelper).validateReservationDates(reservationRequest);

            InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                    () -> reservationServiceImpl.createReservation(reservationRequest, user, accommodationId));

            assertEquals(InvalidDateRangeError.ORDER.getMessage(), exception.getMessage());

            verify(reservationServiceHelper).validateReservationDates(reservationRequest);
            verifyNoInteractions(accommodationServiceHelper);
        }
    }
}