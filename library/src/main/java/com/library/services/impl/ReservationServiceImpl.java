package com.library.services.impl;

import com.library.dtos.reservation.ReservationResponseDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Reservation;
import com.library.models.entities.User;
import com.library.models.entities.enums.ReservationStatus;
import com.library.publisher.BookAvailabilityPublisher;
import com.library.models.repositories.ReservationRepository;
import com.library.services.IBookService;
import com.library.services.IReservationService;
import com.library.services.IUserService;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationServiceImpl implements IReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationRepository repository;

    @Autowired
    private IUserService userService;

    @Autowired
    private IBookService bookService;

    @Autowired
    private BookAvailabilityPublisher bookAvailabilityPublisher;

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO findById(Long id) {
        Reservation entity = findReservationOrThrow(id);
        return new ReservationResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> findByUser(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(ReservationResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponseDTO> findByBook(Long bookId, Pageable pageable) {
        return repository.findByBookIdAndStatus(bookId, ReservationStatus.ACTIVE, pageable)
                .map(ReservationResponseDTO::new);
    }

    @Override
    @Transactional
    public ReservationResponseDTO create(Long bookId) {
        User user = userService.authenticated();
        Book book = bookService.getBook(bookId);

        log.info("Creating reservation | userId={} | bookId={} | book={}",
                user.getId(), bookId, book.getTitle());

        validateReservation(user, book, bookId);

        Reservation entity = buildReservation(user, book);
        entity = repository.save(entity);

        log.info("Reservation created successfully | reservationId={} | userId={} | bookId={}",
                entity.getId(), user.getId(), bookId);

        return new ReservationResponseDTO(entity);
    }

    @Override
    @Transactional
    public ReservationResponseDTO cancel(Long id) {
        log.info("Cancelling reservation | reservationId={}", id);

        Reservation entity = findReservationOrThrow(id);

        validateCancellation(entity);

        entity.setStatus(ReservationStatus.CANCELLED);
        entity = repository.save(entity);

        log.info("Reservation cancelled successfully | reservationId={}", id);

        return new ReservationResponseDTO(entity);
    }

    @Override
    @Transactional
    public void notifyInterestedUsers(Book book) {
        List<Reservation> activeReservations = repository
                .findByBookIdAndStatus(book.getId(), ReservationStatus.ACTIVE);

        log.info("Notifying interested users | bookId={} | book={} | reservations={}",
                book.getId(), book.getTitle(), activeReservations.size());

        if (activeReservations.isEmpty()) {
            log.info("No active reservations found | bookId={}", book.getId());
            return;
        }

        for (Reservation reservation : activeReservations) {
            publishNotification(reservation, book);
            reservation.setStatus(ReservationStatus.NOTIFIED);
        }

        repository.saveAll(activeReservations);

        log.info("All interested users notified | bookId={} | notified={}",
                book.getId(), activeReservations.size());
    }

    private void validateReservation(User user, Book book, Long bookId) {
        if (book.isAvailable()) {
            log.warn("Reservation rejected — book is available | userId={} | bookId={}",
                    user.getId(), bookId);
            throw new BusinessException("Book is available. Borrow it instead of reserving.");
        }

        boolean alreadyReserved = repository.existsByUserIdAndBookIdAndStatus(
                user.getId(), bookId, ReservationStatus.ACTIVE);

        if (alreadyReserved) {
            log.warn("Reservation rejected — user already has active reservation | userId={} | bookId={}",
                    user.getId(), bookId);
            throw new BusinessException("User already has an active reservation for this book");
        }
    }

    private void validateCancellation(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            log.warn("Cancellation rejected — reservation is not active | reservationId={} | status={}",
                    reservation.getId(), reservation.getStatus());
            throw new BusinessException("Reservation is not active");
        }
    }

    private Reservation buildReservation(User user, Book book) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.ACTIVE);
        return reservation;
    }

    private Reservation findReservationOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reservation not found | id={}", id);
                    return new ResourceNotFoundException("Resource not found");
                });
    }

    private void publishNotification(Reservation reservation, Book book) {
        String userEmail = reservation.getUser().getEmail();
        String userName = reservation.getUser().getName();

        bookAvailabilityPublisher.publishBookAvailable(
                book.getId(),
                book.getTitle(),
                userEmail,
                userName
        );

        log.info("BookAvailable event published | bookId={} | userEmail={}",
                book.getId(), userEmail);
    }
}