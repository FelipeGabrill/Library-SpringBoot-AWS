package com.library.services.impl;

import com.library.dtos.reservation.ReservationResponseDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Reservation;
import com.library.models.entities.User;
import com.library.models.entities.enums.ReservationStatus;
import com.library.models.repositories.ReservationRepository;
import com.library.publisher.BookAvailabilityPublisher;
import com.library.services.IBookService;
import com.library.services.IUserService;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.factories.BookFactory;
import com.library.factories.ReservationFactory;
import com.library.factories.UserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @InjectMocks
    private ReservationServiceImpl service;

    @Mock
    private ReservationRepository repository;

    @Mock
    private IUserService userService;

    @Mock
    private IBookService bookService;

    @Mock
    private BookAvailabilityPublisher bookAvailabilityPublisher;

    private long existingId;
    private long nonExistingId;
    private long bookId;
    private User user;
    private Book unavailableBook;
    private Reservation reservation;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 99L;
        bookId = 1L;
        user = UserFactory.createUser();
        unavailableBook = BookFactory.createUnavailableBook();
        reservation = ReservationFactory.createReservation();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByIdShouldReturnReservationResponseDTOWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(reservation));

        ReservationResponseDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
    }

    @Test
    void findByUserShouldReturnPageOfReservationResponseDTO() {
        Page<Reservation> page = new PageImpl<>(List.of(reservation));
        when(repository.findByUserId(1L, pageable)).thenReturn(page);

        Page<ReservationResponseDTO> result = service.findByUser(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByBookShouldReturnPageOfActiveReservations() {
        Page<Reservation> page = new PageImpl<>(List.of(reservation));
        when(repository.findByBookIdAndStatus(bookId, ReservationStatus.ACTIVE, pageable)).thenReturn(page);

        Page<ReservationResponseDTO> result = service.findByBook(bookId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void createShouldReturnReservationResponseDTOWhenBookUnavailable() {
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(unavailableBook);
        when(repository.existsByUserIdAndBookIdAndStatus(user.getId(), bookId, ReservationStatus.ACTIVE))
                .thenReturn(false);
        when(repository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponseDTO result = service.create(bookId);

        assertNotNull(result);
        verify(repository).save(any(Reservation.class));
    }

    @Test
    void createShouldThrowBusinessExceptionWhenBookIsAvailable() {
        Book available = BookFactory.createBook();
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(available);

        assertThrows(BusinessException.class, () -> service.create(bookId));
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void createShouldThrowBusinessExceptionWhenUserAlreadyHasActiveReservation() {
        when(userService.authenticated()).thenReturn(user);
        when(bookService.getBook(bookId)).thenReturn(unavailableBook);
        when(repository.existsByUserIdAndBookIdAndStatus(user.getId(), bookId, ReservationStatus.ACTIVE))
                .thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(bookId));
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void cancelShouldReturnCancelledReservationWhenActive() {
        when(repository.findById(existingId)).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponseDTO result = service.cancel(existingId);

        assertNotNull(result);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void cancelShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.cancel(nonExistingId));
    }

    @Test
    void cancelShouldThrowBusinessExceptionWhenReservationNotActive() {
        Reservation cancelled = ReservationFactory.createCancelledReservation();
        when(repository.findById(existingId)).thenReturn(Optional.of(cancelled));

        assertThrows(BusinessException.class, () -> service.cancel(existingId));
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void notifyInterestedUsersShouldNotifyAndUpdateStatusWhenReservationsExist() {
        Reservation active = ReservationFactory.createReservation(1L, user, unavailableBook, ReservationStatus.ACTIVE);
        when(repository.findByBookIdAndStatus(unavailableBook.getId(), ReservationStatus.ACTIVE))
                .thenReturn(List.of(active));

        service.notifyInterestedUsers(unavailableBook);

        assertEquals(ReservationStatus.NOTIFIED, active.getStatus());
        verify(bookAvailabilityPublisher).publishBookAvailable(
                anyLong(), anyString(), anyString(), anyString());
        verify(repository).saveAll(anyList());
    }

    @Test
    void notifyInterestedUsersShouldDoNothingWhenNoActiveReservations() {
        when(repository.findByBookIdAndStatus(unavailableBook.getId(), ReservationStatus.ACTIVE))
                .thenReturn(List.of());

        service.notifyInterestedUsers(unavailableBook);

        verify(bookAvailabilityPublisher, never())
                .publishBookAvailable(anyLong(), anyString(), anyString(), anyString());
        verify(repository, never()).saveAll(anyList());
    }
}
